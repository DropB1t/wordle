package wordle.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import wordle.utils.*;

public class ShareController implements AutoCloseable {
    private MulticastSocket shareSocket;
    private DatagramPacket datagram;
    private byte[] outBuffer;

    public ShareController(String groupString, int port) throws IOException {
        this.shareSocket = new MulticastSocket(port);

        InetAddress group = InetAddress.getByName(groupString);

        shareSocket.setReuseAddress(true);
        shareSocket.joinGroup(group);
    }

    synchronized public void send(String msg){
        try {
            this.outBuffer = msg.getBytes("UTF-8");
            this.datagram = new DatagramPacket(outBuffer, outBuffer.length);
            System.out.println("Sending UDP");
            shareSocket.send(datagram);
            System.out.println("Sended UDP");
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
