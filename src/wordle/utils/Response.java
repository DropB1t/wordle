package wordle.utils;

public class Response {
    Code code;
    String payload;
    
    public Response(Code code, String payload) {
        this.code = code;
        this.payload = payload;
    }

    public Code getCode() {
        return code;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Response [code=" + code + ", payload=" + payload + "]";
    }
    
}
