package switchexpr;

public class SwitchExpressionFoo {

    public enum Currency {
        YUAN,
        DOLLAR,
        POUND
    }

    public static double getRate(Currency currency) {
        return switch (currency) {
            case YUAN -> 0.14;
            case DOLLAR -> 1;
            case POUND -> 1.32;
        };
    }

}
