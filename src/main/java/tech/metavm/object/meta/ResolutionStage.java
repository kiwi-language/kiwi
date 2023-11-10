package tech.metavm.object.meta;

public enum ResolutionStage {

    INIT(0),

    SIGNATURE(2),

    DECLARATION(3),

    DEFINITION(4);

    private final int code;

    ResolutionStage(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public boolean isBefore(ResolutionStage stage) {
        return code < stage.code;
    }

    public boolean isAfter(ResolutionStage stage) {
        return code > stage.code;
    }

    public boolean isAfterOrAt(ResolutionStage stage) {
        return code >= stage.code;
    }

    public boolean isBeforeOrAt(ResolutionStage stage) {
        return code <= stage.code;
    }

}
