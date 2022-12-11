package wordle.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import wordle.utils.Util;

public class ServerController implements AutoCloseable {
    private AtomicBoolean loop = new AtomicBoolean(true);

    private final ServerSocketChannel serverSocket;
    private final InetSocketAddress addr;
    private final Selector selector;

    private final ShareController shareSocket;

    private final ExecutorService workersPool;
    private final ConcurrentHashMap<SocketChannel, ByteBuffer> connectedUsers = new ConcurrentHashMap<>();

    private final GameSession game;

    public ServerController(String host, int port, ExecutorService workersPool, ShareController shareSocket, GameSession game) throws IOException {
        this.game = game;
        this.workersPool = workersPool;
        this.shareSocket = shareSocket;
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

            game.updateSession();
            connectedUsers.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());
        }
    }

    private void accept(final SelectionKey key){
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socket = socketChannel.accept();
            socket.configureBlocking(false);
            socket.register(key.selector(), SelectionKey.OP_READ);
            connectedUsers.put(socket, ByteBuffer.allocateDirect(1024));
        } catch (IOException e) {
            Util.printException(e);
        }
    }

    private void write(final SelectionKey key){
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = connectedUsers.get(socket);

        try {
            socket.write(byteBuffer);
        } catch (IOException e) {
            Util.printException(e);
        }

        while (!byteBuffer.hasRemaining()) {
            byteBuffer.compact();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void read(final SelectionKey key){
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = connectedUsers.get(socket);
        try {
            int data = socket.read(byteBuffer);
            if (data < 0) {
                closeSocket(socket);
                connectedUsers.remove(socket);
                return;
            }
            //System.out.println(byteBuffer.toString());
            
            byteBuffer.flip();
            byte [] strBytes = new byte[data];
            byteBuffer.get(strBytes);

            String res = new String(strBytes);                
            System.out.println(res);

            byteBuffer.flip();
            socket.configureBlocking(false);
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            connectedUsers.remove(socket);
        } catch (IOException e) {
            Util.printException(e);
        }
    }

    private void closeSocket(final SocketChannel socket) {
        try {
            socket.close();
        } catch (IOException e) {
            Util.printException(e);
        }
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

}
