package primitives;

public class SmallIntFoo {

    public static final byte[] bytes = new byte[] {1, 2, 3};

    public static byte toByte(int v) {
        return (byte) v;
    }

    public static short addShorts(short s1, short s2) {
        return (short) (s1 + s2);
    }

    public static double addShortAndDouble(short s1, double d1) {
        return s1 + d1;
    }

    public static int shortShiftRight(short value, int shift) {
        return value >> shift;
    }

    public static byte byteArrayGet(int index) {
        return bytes[index];
    }

    public static void byteArraySet(int index, byte value) {
        bytes[index] = value;
    }

}
