public enum Currency {
    YUAN(0.14),
    DOLLAR(1),
    POUND(1.32)

    ;

    private double rate;

    Currency(double rate) {
        this.rate = rate;
    }

}
