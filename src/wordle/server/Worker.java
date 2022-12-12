package wordle.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import wordle.utils.Util;

public class Worker implements Runnable {

    private final ConcurrentHashMap<SocketChannel, User> connectedUsers;
    private final ShareController shareSocket;
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
        
    }
    
}
