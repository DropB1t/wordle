package wordle.utils;

import java.io.IOException;

public class Request {
    OptType type;
    String payload;
    
    public Request(OptType type, String payload) throws IOException {
        if (type == OptType.Register || type == OptType.Login) {
            throw new IOException("Invalid operation type");
        }
        this.type = type;
        this.payload = payload;
    }

    public Request(OptType type, String username, String psw) throws IOException {
        if (type != OptType.Register && type != OptType.Login) {
            throw new IOException("Invalid operation type");
        }
        this.type = type;
        this.payload = username + " " + psw ;
    }

    public OptType getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Request [type=" + type + ", payload=" + payload + "]";
    }

}
