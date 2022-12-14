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

import wordle.utils.Code;
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
    private boolean loop = true;

    public Client(String host, int port, ShareWatcher watcher) throws IOException {
        this.watcher = watcher;
        this.scanner = new Scanner(System.in);
        this.buff = ByteBuffer.allocateDirect(2048);

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

        if(!access()){
            close();
            return;
        }

        do {
            try {
                String opt = getMainMenuInput();
                switch (opt) {
                    case "1":
                        play();
                        break;
                    case "2":
                        guess();
                        break;
                    case "3":
                        stats();
                        break;
                    case "4":
                        watcher.printShare();
                        break;
                    case "5":
                        logout();
                        break;
                    default:
                        break;
                }
            } catch (ClosedChannelException e) {
                close();
            }
        } while (loop);

        close();
    }

    private String getMainMenuInput(){
        AppText.menuText();
        boolean bool;
        String in;
        do {
            System.out.print(">");
            in = scanner.nextLine();
            bool = in.isEmpty() | !in.matches("^[1-6]$");
            if (in.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty choice is not allowed" + Util.ConsoleColors.RESET);
            if (!in.isEmpty() && bool)
                System.out.println(Util.ConsoleColors.RED + "Choose valid menu entry" + Util.ConsoleColors.RESET);
        } while (bool);
        return in;
    }

    private void play() throws IOException{
        send(new Request(OptType.Play, ""));
        Response res = receive();
        System.out.println(res.getPayload());
    }

    private void guess() throws IOException{
        send(new Request(OptType.SendWord, getGuess().trim().toLowerCase()));
        Response res = receive();
        System.out.println(res.getPayload());
        if (res.getCode() == Code.Win || res.getCode() == Code.Lose) {
            share();
        }
    }

    private String getGuess(){
        String in;
        do {
            System.out.print(">");
            in = scanner.nextLine();
            if (in.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty choice is not allowed" + Util.ConsoleColors.RESET);
        } while (in.isEmpty());
        return in;
    }

    private void share() throws IOException{
        String opt = getShare();
        if(opt.equals("yes")){
            send(new Request(OptType.Share, ""));
            Response res = receive();
            System.out.println(res.getPayload());
        }
    }

    private String getShare(){
        AppText.shareText();
        boolean bool;
        String in;
        do {
            System.out.print(">");
            in = scanner.nextLine();
            bool = in.isEmpty() || !in.equals("yes") && !in.equals("no");
            if (in.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty choice is not allowed" + Util.ConsoleColors.RESET);
            if (!in.isEmpty() && bool)
                System.out.println(Util.ConsoleColors.RED + "Choose valid entry [yes/no]" + Util.ConsoleColors.RESET);
        } while (bool);
        return in;
    }

    private void stats() throws IOException{
        send(new Request(OptType.SendStats, ""));
        Response res = receive();
        if (res.getCode() != Code.Success){
            System.out.println(Util.ConsoleColors.RED + "Error in retrieving stats" + Util.ConsoleColors.RESET);
            return;
        }
        System.out.println(res.getPayload());
    }

    private void logout() throws IOException{
        send(new Request(OptType.Logout, ""));
        loop = false;
    }

    private boolean access() throws IOException{
        Response res;
        do {
            switch (getAccessMenuInput()) {
                case "1":
                    res = accessOpt(OptType.Register);
                    break;
                case "2":
                    res = accessOpt(OptType.Login);
                    break;
                case "3":
                    send(new Request(OptType.Logout, ""));
                    return false;
                default:
                    return false;
            }
            if (res.getCode() == Code.SuccLog || res.getCode() == Code.SuccReg)
                System.out.println(res.getPayload());
            if (res.getCode() == Code.ErrReg || res.getCode() == Code.ErrLog || res.getCode() == Code.ErrPsw)
                System.out.println(res.getPayload());
        } while (res.getCode() != Code.SuccLog);
        return true;
    }

    private String getAccessMenuInput() {
        AppText.accessText();
        boolean bool;
        String in;
        do {
            System.out.print(">");
            in = scanner.nextLine();
            bool = in.isEmpty() || !in.matches("^[1-3]$");
            if (in.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty choice is not allowed" + Util.ConsoleColors.RESET);
            if (!in.isEmpty() && bool)
                System.out.println(Util.ConsoleColors.RED + "Choose valid menu entry" + Util.ConsoleColors.RESET);
        } while (bool);
        return in;
    }

    private Response accessOpt(OptType opt) throws IOException{
        boolean bool;
        String username,psw;
        do {
            System.out.print("Username:");
            username = scanner.nextLine().trim();
            bool = username.matches(".*([ \t]).*") || username.isEmpty();
            if(username.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty username is not allowed" + Util.ConsoleColors.RESET);
            if(username.matches(".*([ \t]).*"))
                System.out.println(Util.ConsoleColors.RED + "Whitespaces are not allowed" + Util.ConsoleColors.RESET);
        } while (bool);
        do {
            System.out.print("Password:");
            psw = scanner.nextLine().trim();
            bool = psw.matches(".*([ \t]).*") || psw.isEmpty();
            if(psw.isEmpty())
                System.out.println(Util.ConsoleColors.RED + "Empty password is not allowed" + Util.ConsoleColors.RESET);
            if(psw.matches(".*([ \t]).*"))
                System.out.println(Util.ConsoleColors.RED + "Whitespaces are not allowed" + Util.ConsoleColors.RESET);
        } while (bool);
        send(new Request(opt, username, Util.hash(psw)));
        return receive();
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
        if (read < 0) {
            System.out.print(Util.ConsoleColors.RED + "\n| Forced shutdown of the client. Connection closed. |\n" + Util.ConsoleColors.RESET);
            System.exit(1);
        }
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
