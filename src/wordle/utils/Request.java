package wordle.utils;

public class Request {
    OptType type;
    String payload;
    
    public Request(OptType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public OptType getType() {
        return type;
    }

    public void setType(OptType type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Request [type=" + type + ", payload=" + payload + "]";
    }

}
