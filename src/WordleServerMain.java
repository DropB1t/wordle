import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class WordleServerMain {
    public static void main(String[] args) {
        Properties prop = new Properties();
        String fileName = "server.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (IOException e) {
            Util.printException(e);
        }
        System.out.println(prop.getProperty("host"));
        System.out.println(Integer.parseInt(prop.getProperty("port")));

    }
}
