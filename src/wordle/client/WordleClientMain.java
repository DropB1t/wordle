package wordle.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import wordle.utils.OptType;
import wordle.utils.Request;
import wordle.utils.Response;
import wordle.utils.Util;

public class WordleClientMain {
    public static void main(String[] args) {

        TypeAdapter<Request> reqAdapter = new Gson().getAdapter(Request.class);
        TypeAdapter<Response> resAdapter = new Gson().getAdapter(Response.class);

        final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Request.class, reqAdapter)
        .registerTypeAdapter(Response.class, resAdapter)
        .enableComplexMapKeySerialization()
        .serializeNulls()
        .create();

        try (SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 10080))) {
            Scanner scanner = new Scanner(System.in);
            ByteBuffer buff = ByteBuffer.allocateDirect(1024);

            while (true) {
                String msg = getInput(scanner);

                buff.clear();
                Request req = new Request(OptType.Register, msg, "1234");
                String jsonReq = gson.toJson(req, Request.class);
                buff.put(jsonReq.getBytes());
                buff.flip();
                client.write(buff);

                if (msg.equals("exit()"))
                    break;

                buff.clear();
                int read = client.read(buff);
                
                buff.flip();
                byte [] strBytes = new byte[read];
                buff.get(strBytes);

                String jsonRes = new String(strBytes);
                Response res = gson.fromJson(jsonRes, Response.class);            
                System.out.println(res.toString());
            }

        } catch (IOException e) {
            Util.printException(e);
        }
    }
    
    private static String getInput(Scanner scanner) {
        String req;
        do {
            System.out.print(">");
            req = scanner.nextLine();
            if (req.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty echo messagges are not allowed" + Util.ConsoleColors.RESET);
        } while (req.isEmpty());
        return req;
    }
}
