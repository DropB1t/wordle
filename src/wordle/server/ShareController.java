package wordle.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import wordle.utils.*;

public class ShareController implements AutoCloseable {
    private MulticastSocket shareSocket;
    private DatagramPacket datagram;
    private InetAddress group;
    private int port;

    public ShareController(String groupString, int port) throws IOException {
        
        this.port = port;
        group = InetAddress.getByName(groupString);
        this.shareSocket = new MulticastSocket(port);

        shareSocket.setReuseAddress(true);
        shareSocket.joinGroup(group);

    }

    public void send(String msg){
        try {
            //System.out.println(msg);
            byte[] outBuffer = msg.getBytes();
            this.datagram = new DatagramPacket(outBuffer, outBuffer.length, group, port);
            shareSocket.send(datagram);
        } catch (IOException e) {
            Util.printException(e);
            System.exit(1);
        }
    }

    @Override
    public void close() throws Exception {
        shareSocket.close();
    }

}
