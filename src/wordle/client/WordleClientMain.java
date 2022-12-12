package wordle.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import wordle.utils.GsonByteBufferTypeAdapter;
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
            String res = null;
            Scanner scanner = new Scanner(System.in);
            ByteBuffer outBuffer = ByteBuffer.allocate(1024);

            while (true) {
                String msg = getInput(scanner);

                String json = gson.toJson(new Request(OptType.Login, msg), Request.class);
                //System.out.println(json);
                client.write(ByteBuffer.wrap(json.getBytes()));

                if (msg.equals("exit()"))
                    break;

                outBuffer.clear();
                int read = client.read(outBuffer);
                
                outBuffer.flip();
                byte [] strBytes = new byte[read];
                outBuffer.get(strBytes);

                res = new String(strBytes);                
                System.out.println(res);
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
