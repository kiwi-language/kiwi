package primitives;

public class PrimitiveConversionFoo {

    public static double value;

    public static final double[] values = new double[] { 1, 2.0, 3};

    public static double add(long v, double d) {
        return v + d;
    }

    public static double setValue(long v) {
        return value = v;
    }

    public static double inc(long v) {
        return add(v, 1);
    }

    public static double getValue(int index) {
        return values[index];
    }

}
