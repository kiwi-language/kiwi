package misc;

public enum Currency2 {

    YUAN(0.14),
    DOLLAR(1)

    ;

    private final double rate;

    Currency2(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}
