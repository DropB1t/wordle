package wordle.client;

import wordle.utils.Util;

public class ClientShutdownHook extends Thread {
    private final Client client;

    public ClientShutdownHook(Client client) {
        this.client = client;
    }
    
    @Override
    public void run() {
        try {
            this.client.close();
        } catch (Exception e) {
            Util.printException(e);
        }
        System.out.print(Util.ConsoleColors.RED + "\n| Forced shutdown of the client |\n" + Util.ConsoleColors.RESET);
    }
    
}
