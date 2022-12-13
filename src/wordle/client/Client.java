package wordle.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import wordle.utils.OptType;
import wordle.utils.Request;
import wordle.utils.Response;
import wordle.utils.Util;

public class Client implements AutoCloseable {
    private SocketChannel client;
    private ShareWatcher watcher;
    private Scanner scanner;
    private ByteBuffer buff;
    private Gson gson;

    public Client(String host, int port, ShareWatcher watcher) throws IOException {
        this.watcher = watcher;
        this.scanner = new Scanner(System.in);
        this.buff = ByteBuffer.allocateDirect(1024);

        TypeAdapter<Request> reqAdapter = new Gson().getAdapter(Request.class);
        TypeAdapter<Response> resAdapter = new Gson().getAdapter(Response.class);

        gson = new GsonBuilder()
                .registerTypeAdapter(Request.class, reqAdapter)
                .registerTypeAdapter(Response.class, resAdapter)
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .create();

        InetSocketAddress addr = new InetSocketAddress(host, port);
        client = SocketChannel.open(addr);
    }

    public void run() throws Exception {
        AppText.welcomeText();
        while (true) {
            try {
                String msg = getAccessInput();
                
                Request req = new Request(OptType.Register, msg, "1234");
                send(req);

                if (req.getType() == OptType.Logout)
                    break;

                Response res = receive();

                System.out.println(res.toString());
            } catch (ClosedChannelException e) {
                close();
            }
        }
        close();
    }

    private String getAccessInput() {
        String req;
        do {
            System.out.print(">");
            req = scanner.nextLine();
            if (req.isEmpty())
                System.out.println(
                        Util.ConsoleColors.RED + "Empty echo messagges are not allowed" + Util.ConsoleColors.RESET);
        } while (req.isEmpty());
        return req;
    }

    private void send(Request req) throws IOException{
        String jsonReq = gson.toJson(req, Request.class);
        buff.clear();
        buff.put(jsonReq.getBytes());
        buff.flip();
        client.write(buff);
    }

    private Response receive() throws IOException{
        buff.clear();
        int read = client.read(buff);
        buff.flip();
        byte[] strBytes = new byte[read];
        buff.get(strBytes);
        String jsonRes = new String(strBytes);
        return gson.fromJson(jsonRes, Response.class);
    }

    @Override
    public void close() throws Exception {
        watcher.stop();
        watcher.close();
        client.close();
    }

}
