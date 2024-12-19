public enum Currency {
    EURO(1.04),
    YUAN(0.14),
    DOLLAR(1),
    POUND(1.32)

    ;

    private double rate;

    Currency(double rate) {
        this.rate = rate;
    }

    public double __rate__() {
        return switch (this) {
            case EURO -> 1.04;
            case YUAN -> 0.14;
            case DOLLAR -> 1;
            case POUND -> 1.32;
        };
    }
}
