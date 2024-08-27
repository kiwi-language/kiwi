public enum Currency {
    YUAN(0.14),
    DOLLAR(1),
    POUND(1.32)

    ;

    private double rate;

    Currency(double ratio) {
        this.rate = ratio;
    }

    private double __rate__() {
        return switch (this) {
            case YUAN -> 0.14;
            case DOLLAR -> 1;
            case POUND -> 1.32;
        };
    }
}
