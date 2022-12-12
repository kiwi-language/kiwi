package tech.metavm.dto;

public enum InternalErrorCode {

    PROXY_CIRCULAR_REF(9110001,
            "Proxy initialization recurses. Current method: {}"),

    MODEL_TYPE_MISMATCHED(9110002,
            "Model type mismatch. Expected type: {}. Actual Type: {}"),

    INVALID_ID(9110003,
            "Invalid id: {}. {}"),

    ;

    private final int code;
    private final String message;

    InternalErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }


}
