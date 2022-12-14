package wordle.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import wordle.utils.Util;

/**
 * ResourceController is responsible of saving and loading persistent state of registered Users
 */
public class ResourceController {
    private static final String folder = "./resources/";

    private final Gson gson;

    public ResourceController() {
        TypeAdapter<User> userAdapter = new Gson().getAdapter(User.class);
        TypeAdapter<GameSession> gameAdapter = new Gson().getAdapter(GameSession.class);

        gson = new GsonBuilder()
        .registerTypeAdapter(User.class, userAdapter)
        .registerTypeAdapter(GameSession.class, gameAdapter)
        .enableComplexMapKeySerialization()
        .serializeNulls()
        .setPrettyPrinting()
        .create();

    }
    
    public boolean checkIfUserExist(String username){
        return new File(folder+Util.hash(username)+".json").isFile();
    }

    /**
     * Load user object from json file
     */
    public User getUser(String username){
        User user = null;
        try {
            FileReader reader = new FileReader(folder + Util.hash(username) + ".json");
            user = gson.fromJson(reader, User.class);
        } catch (IOException e) {
            Util.printException(e);
        }
        return user;
    }

    /**
     * Saves users state into json file
     */
    public void saveUser(User user){
        try {
            FileWriter writer = new FileWriter(folder + Util.hash(user.getUsername()) + ".json");
            gson.toJson(user, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Util.printException(e);
        }
    }

}
