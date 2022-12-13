package wordle.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import wordle.utils.Util;

public class ResourceController {
    private static final String folder = "./resources/";

    private final Gson gson;
    private MessageDigest digest;

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

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Util.printException(e);
        }
    }
    
    public boolean checkIfUserExist(String username){
        return new File(folder+hash(username)+".json").isFile();
    }

    public void saveUser(User user){
        try {
            FileWriter writer = new FileWriter(folder + hash(user.getUsername()) + ".json");
            gson.toJson(user, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Util.printException(e);
        }
    }

    private String hash(String str){
        return bytesToHex(digest.digest(str.getBytes()));
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
