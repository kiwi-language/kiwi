import org.metavm.api.EntityField;

public enum Currency {
    EURO(1.04),
    YUAN(0.14),
    @EntityField(tag = 0)
    USD(1),
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
            case USD -> 1;
            case POUND -> 1.32;
        };
    }
}
