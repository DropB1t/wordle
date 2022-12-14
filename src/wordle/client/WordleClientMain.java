package wordle.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import wordle.utils.Util;

public class WordleClientMain {
    private static final String config = "client.config";
    private static String host;
    private static String mcgroup;
    private static int port;
    private static int mcport;

    public static void main(String[] args) {
        loadProps();
        try (ShareWatcher watcher = new ShareWatcher(mcgroup, mcport); Client client = new Client(host, port, watcher)) {
            Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(client));
            new Thread(watcher).start();
            client.run();
        } catch (IOException e) {
            Util.printException(e);
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
        } catch (NumberFormatException e) {
            System.err.println("A config field is not parsable to an integer");
            System.exit(1);
        }
    }
}
