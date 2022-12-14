package wordle.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import wordle.utils.Response;
import wordle.utils.Code;
import wordle.utils.Request;
import wordle.utils.Util;

/**
 * Server Controller open and bind ServerSocketChannel in non-blocking mode to accept, read request, dispatch them to workers,
 * and write/send Response to connected Clients
 * <p>
 * Connected clients sockets are stored in a hashmap and mapped to their clientID that are relative to current session
 * <p>
 * Logged users are mapped in a different structure ( ConcurrentHashMap ) for supporting full concurrency of retrievals
 * and high expected concurrency for updates performed by Workers in workersPool
 */
public class ServerController implements AutoCloseable {
    private static Integer clientID = 0;

    private AtomicBoolean loop = new AtomicBoolean(true);

    private final ServerSocketChannel serverSocket;
    private final InetSocketAddress addr;
    private final Selector selector;

    private final ShareController shareSocket;

    private final ExecutorService workersPool;
    private final HashMap<SocketChannel, Integer> connectedClients = new HashMap<>();
    private final ConcurrentHashMap<Integer, User> loggedUsers = new ConcurrentHashMap<>();
    private final ByteBuffer buff;


    private final WordManager wordSession;

    private final Gson gson;

    public ServerController(String host, int port, ExecutorService workersPool, ShareController shareSocket, WordManager game) throws IOException {
        this.wordSession = game;
        this.workersPool = workersPool;
        this.shareSocket = shareSocket;
        this.buff = ByteBuffer.allocateDirect(2048);

        TypeAdapter<Request> reqAdapter = new Gson().getAdapter(Request.class);
        TypeAdapter<Response> resAdapter = new Gson().getAdapter(Response.class);

        /**
         * Gson instance with registered type adapters to fetch/put Requests/Responses from ByteBuffer
         */
        gson = new GsonBuilder()
        .registerTypeAdapter(Request.class, reqAdapter)
        .registerTypeAdapter(Response.class, resAdapter)
        .enableComplexMapKeySerialization()
        .serializeNulls()
        .create();

        this.addr = new InetSocketAddress(host, port);
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(addr);
        this.serverSocket.configureBlocking(false);

        this.selector = Selector.open();
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() throws IOException {
        System.out.println(Util.ConsoleColors.GREEN + "Server " + addr.getAddress() + " running on " + addr.getPort() + Util.ConsoleColors.RESET);
        int readyOpt;
        while (loop.get()) {
            readyOpt = selector.select(1000);

            if (readyOpt != 0) {
                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext();) {
                    final SelectionKey key = it.next();
                    it.remove();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isWritable()) {
                            write(key);
                        } else if (key.isReadable()) {
                            read(key);
                        }
                    }
                }
            }

            /**
             * After key for loop selection Controller removes all closed socket channels from both structures (connectedClients & loggedUsers)
             * If a new Secret Word was generated we update the state of users last guess word so they can guess the new current secret word
             */
            connectedClients.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());
            synchronized(loggedUsers){loggedUsers.keySet().removeIf((id) -> !connectedClients.containsValue(id));}

            if (wordSession.updateSession())
                synchronized(loggedUsers){ loggedUsers.forEach((id,user) -> user.setLastGuessedWord(false)); }
        }
    }

    /**
     * Server Controller accept new socket channels, set them to non blocking, register socket to the main selector with read operation
     * and put the accepted socket into connectedClients with his clientID
     */
    private void accept(final SelectionKey key) throws IOException{
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socket = socketChannel.accept();
        socket.configureBlocking(false);
        socket.register(key.selector(), SelectionKey.OP_READ);
        connectedClients.put(socket, getClientID());
        incClientID();
    }

    /**
     * Server Controller send Response to ready to read clients if the response is already completed. This is done by checking Future,
     * attached to the key, object returned by Callable Worker
     */
    private void write(final SelectionKey key) throws IOException{
        final SocketChannel socket = (SocketChannel) key.channel();
        final Integer clientID = connectedClients.get(socket);

        Future<?> f = (Future<?>) key.attachment();
        Response res;

        if (!f.isDone()){
            key.attach(f);
            return;
        }

        try {
            res = (Response)f.get(1000,TimeUnit.MILLISECONDS);
        } catch ( TimeoutException e) {
            key.attach(f);
            return;
        }catch ( InterruptedException | ExecutionException  e) {
            Util.printException(e);
            return;
        }

        if (res.getCode() == Code.Logout) {
            System.out.println(Util.ConsoleColors.YELLOW + "Logout client with ID: " + clientID + Util.ConsoleColors.RESET);
            closeSocket(socket);
            return;
        }

        System.out.println(res.toString());

        try {
            String jsonRes = gson.toJson(res, Response.class);
            buff.clear();
            buff.put(jsonRes.getBytes());
            buff.flip();
            while (buff.hasRemaining())
                socket.write(buff);
            buff.compact();
            key.interestOps(SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            closeSocket(socket);
        }
    }

    /**
     * Read the client Request and dispatch it to a workers pool and saving the Future Response by attaching it to the key
     * After which sets this key's interest set to Write Operation
     */
    private void read(final SelectionKey key) throws IOException{
        final SocketChannel socket = (SocketChannel) key.channel();
        final Integer clientID = connectedClients.get(socket);
        try {
            buff.clear();
            int data = socket.read(buff);
            if (data < 0) {
                closeSocket(socket);
                return;
            }
            //System.out.println(buff.toString());
            
            buff.flip();
            byte [] strBytes = new byte[data];
            buff.get(strBytes);
            String jsonReq = new String(strBytes);
            Request req = gson.fromJson(jsonReq, Request.class);

            Future<Response> f = workersPool.submit(new Worker(req, clientID, loggedUsers, wordSession, shareSocket));

            key.attach(f);
            System.out.println(req.toString());
            
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            closeSocket(socket);
        }
    }

    private void closeSocket(final SocketChannel socket) throws IOException {
        synchronized(loggedUsers){loggedUsers.remove(connectedClients.get(socket));}
        socket.close();
    }

    @Override
    public void close() throws Exception {
        this.selector.close();
        this.serverSocket.close();
        this.shareSocket.close();
    }

    public void stop() {
        this.loop.set(false);
    }

    private static Integer getClientID() {
        return clientID;
    }

    private static Integer incClientID() {
        return clientID += 1;
    }

}
