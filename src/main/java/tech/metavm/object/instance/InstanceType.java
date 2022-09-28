package tech.metavm.object.instance;

public enum InstanceType {
    ATOM(0),
    SET(1),

    ;

    private final byte code;

    InstanceType(int code) {
        this.code = (byte) code;
    }

    public byte code() {
        return code;
    }

}
