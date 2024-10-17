package utils;

public class PrimitiveUtilMethods {

    public static int numberOfLeadingZeros(long l) {
        return Long.numberOfLeadingZeros(l);
    }

    public static int numberOfTrailingZeros(long l) {
        return Long.numberOfTrailingZeros(l);
    }

    public static int intNumberOfLeadingZeros(int i) {
        return Integer.numberOfLeadingZeros(i);
    }

    public static int intNumberOfTrailingZeros(int i) {
        return Integer.numberOfTrailingZeros(i);
    }

    public static int floatToRawIntBits(float f) {
        return Float.floatToRawIntBits(f);
    }

    public static long doubleToRawLongBits(double d) {
        return Double.doubleToRawLongBits(d);
    }

}
