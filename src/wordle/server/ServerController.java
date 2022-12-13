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


    private final GameSession game;

    private final Gson gson;

    public ServerController(String host, int port, ExecutorService workersPool, ShareController shareSocket, GameSession game) throws IOException {
        this.game = game;
        this.workersPool = workersPool;
        this.shareSocket = shareSocket;
        this.buff = ByteBuffer.allocateDirect(1024);

        TypeAdapter<Request> reqAdapter = new Gson().getAdapter(Request.class);
        TypeAdapter<Response> resAdapter = new Gson().getAdapter(Response.class);

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

            if (game.updateSession())
                synchronized(loggedUsers){ loggedUsers.forEach((k,v) -> v.setPlaying(false)); }

            connectedClients.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());
        }
    }

    private void accept(final SelectionKey key) throws IOException{
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socket = socketChannel.accept();
        socket.configureBlocking(false);
        socket.register(key.selector(), SelectionKey.OP_READ);
        connectedClients.put(socket, getClientID());
        incClientID();
    }

    private void write(final SelectionKey key) throws IOException{
        final SocketChannel socket = (SocketChannel) key.channel();
        //final Integer clientID = connectedClients.get(socket);

        Future<?> f = (Future<?>) key.attachment();
        Response res;

        if (!f.isDone()){
            key.attach(f);
            return;
        }

        try {
            res = (Response)f.get(1000,TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            key.attach(f);
            return;
        }

        if (res.getCode() == Code.Logout) {
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
            Future<Response> f = workersPool.submit(new Worker(req, clientID, loggedUsers, game, shareSocket));
            key.attach(f);
            System.out.println(req.toString());
            
            //socket.configureBlocking(false);
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            closeSocket(socket);
        }
    }

    private void closeSocket(final SocketChannel socket) throws IOException {
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
