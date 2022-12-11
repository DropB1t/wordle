package wordle.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import wordle.utils.Util;

public class Worker implements Runnable {

    private final ConcurrentHashMap<SocketChannel, User> connectedUsers;
    private final  ShareController shareSocket;
    private final SelectionKey key;
    private final GameSession game;
    
    

    public Worker(ConcurrentHashMap<SocketChannel, User> connectedUsers, ShareController shareSocket, SelectionKey key, GameSession game) {
        this.connectedUsers = connectedUsers;
        this.shareSocket = shareSocket;
        this.key = key;
        this.game = game;
    }

    @Override
    public void run() {
        if (key.isAcceptable()) {
            accept();
        } else if (key.isWritable()) {
            write();
        } else if (key.isReadable()) {
            read();
        }
    }

    private void accept(){
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socket = socketChannel.accept();
            socket.configureBlocking(false);
            socket.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            Util.printException(e);
        }
    }

    private void write(){
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = (ByteBuffer) key.attachment();

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

    private void read(){
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        try {
            int data = socket.read(byteBuffer);
            if (data == -1) {
                closeSocket(socket);
                connectedUsers.remove(socket);
            }
            byteBuffer.flip();
            key.attach(byteBuffer);
            socket.configureBlocking(false);
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            Util.printException(e);
        }
    }

    private void closeSocket(final SocketChannel socket) {
        try {
            socket.close();
        } catch (IOException ignore) {

        }
    }
    
}
