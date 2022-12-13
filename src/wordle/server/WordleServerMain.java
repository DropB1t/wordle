package wordle.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import wordle.utils.*;

public class WordleServerMain {
    private static final String config = "server.config";
    private static String host;
    private static String mcgroup;
    private static int port;
    private static int mcport;
    private static int poolsize;
    private static long seed;
    private static long sessionDuration; // In min

    public static void main(String[] args) {

        loadProps();
        ExecutorService workersPool = new WorkersThreadPool(poolsize);
        WordManager wordSession = new WordManager(seed, sessionDuration);

        try (ShareController shareSocket = new ShareController(mcgroup, mcport); ServerController server = new ServerController(host, port, workersPool, shareSocket, wordSession)) {
            Runtime.getRuntime().addShutdownHook(new ServerShutdownHook(server, workersPool));
            server.run();
        } catch (Exception e) {
            Util.printException(e);
        }

    }

    private static void loadProps() {
        Properties prop = new Properties();
        try (FileInputStream ficonfig = new FileInputStream(config)) {
            prop.load(ficonfig);
        } catch (IOException e) {
            Util.printException(e);
            System.exit(1);
        }
        try {
            host = prop.getProperty("host");
            mcgroup = prop.getProperty("mcgroup");
            port = Integer.parseInt(prop.getProperty("port"));
            mcport = Integer.parseInt(prop.getProperty("mcport"));
            poolsize = Integer.parseInt(prop.getProperty("poolsize"));
            seed = Long.parseLong(prop.getProperty("seed"));
            sessionDuration = Long.parseLong(prop.getProperty("session"));
        } catch (NumberFormatException e) {
            System.err.println("A config field is not parsable to an integer");
            System.exit(1);
        }
        System.out.print(Util.ConsoleColors.CYAN);
        System.out.println("\n[Config Parameters]");
        System.out.println(" Host: " + host);
        System.out.println(" Port: " + port);
        System.out.println(" MusticastGroup: " + mcgroup);
        System.out.println(" MulticastPort:" + mcport);
        System.out.println(" ThreadPoolSize: " + poolsize);
        System.out.println(" SessionDuration: " + sessionDuration);
        System.out.println(" Seed: " + seed);
        System.out.println("[=================]\n");
        System.out.print(Util.ConsoleColors.RESET);
    }
}
