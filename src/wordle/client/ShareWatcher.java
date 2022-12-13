package wordle.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import wordle.utils.Util;

public class ShareWatcher implements Runnable, AutoCloseable {

    private AtomicBoolean loop = new AtomicBoolean(true);

    private MulticastSocket shareSocket;
    private DatagramPacket datagram;
    private byte[] buff;

    private List<String> sharedList;

    public ShareWatcher(String groupString, int port) throws IOException {
        InetAddress group = InetAddress.getByName(groupString);
        this.shareSocket = new MulticastSocket(port);
        shareSocket.setReuseAddress(true);
        shareSocket.joinGroup(group);

        buff = new byte[1024];
        datagram = new DatagramPacket(buff, buff.length);

        sharedList = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public void run() {
        String res;
        while(loop.get()){
            try {
                shareSocket.receive(datagram);
                res = new String(datagram.getData(),0,datagram.getLength(),"UTF-8");
                sharedList.add(res);
            } catch (IOException e) {
                Util.printException(e);
            }
        }
    }

    synchronized public void printShare() {
        for (String post : sharedList) {
            System.out.print("\n"+post+"\n");
        }
    }

    public void stop() {
        this.loop.set(false);
    }

    @Override
    public void close() throws Exception {
        shareSocket.close();
    }
    
}
