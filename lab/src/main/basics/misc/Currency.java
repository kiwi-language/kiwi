package misc;

public enum Currency {

    YUAN(0.14),
    DOLLAR(1)

    ;

    private final double rate;

    Currency(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}
