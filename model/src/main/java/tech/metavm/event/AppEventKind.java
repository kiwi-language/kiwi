package tech.metavm.event;

public enum AppEventKind {

    TYPE_CHANGE(1),

    ;

    private final int code;

    AppEventKind(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

}
