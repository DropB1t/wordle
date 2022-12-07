import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main");
        Gson gson = new Gson();
        gson.serializeNulls();
    }
}