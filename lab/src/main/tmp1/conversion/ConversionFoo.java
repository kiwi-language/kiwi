package conversion;

import java.util.function.DoubleSupplier;

public class ConversionFoo {

    private static long value;

    private static double[] values = new double[] {
            1, 2.0, 3
    };

    public static double add(long v, double d) {
        return v * 2 + d;
    }

    public static void inc(double v) {
        value += v;
    }

    public static double inc(long v) {
        return add(v, 1);
    }

    public static double constantOne() {
        return 1;
    }

    public static DoubleSupplier getSupplier() {
        return () -> {
            return value;
        };
    }

    public static DoubleSupplier getSupplier2() {
        return () -> value;
    }

}