package enums;

public enum ProductKind {

    DEFAULT(0),
    VIRTUAL(1),
    HOTEL(2),
    ;

    private final int code;

    ProductKind(int code) {
        this.code = code;
    }

    public static ProductKind fromCode(int code) {
        for (ProductKind kind : values()) {
            if(kind.code == code)
                return kind;
        }
        throw new NullPointerException("Cannot find a ProductKind for code " + code);
    }
}
