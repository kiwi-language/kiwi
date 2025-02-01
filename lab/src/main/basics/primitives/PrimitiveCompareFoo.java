package primitives;

public class PrimitiveCompareFoo {

    public static <T extends Comparable<T>> int compare(T t1, T t2) {
        return t1.compareTo(t2);
    }

    public static int compareString(String s1, String s2) {
        return compare(s1, s2);
    }

    public static int compareByte(byte a, byte b) {
        return compare(a, b);
    }

    public static int compareShort(short a, short b) {
        return compare(a, b);
    }

    public static int compareInt(int a, int b) {
        return compare(a, b);
    }

    public static int compareLong(long a, long b) {
        return compare(a, b);
    }

    public static int compareFloat(float a, float b) {
        return compare(a, b);
    }

    public static int compareDouble(double a, double b) {
        return compare(a, b);
    }

    public static int compareChar(char a, char b) {
        return compare(a, b);
    }

    public static int compareBoolean(boolean a, boolean b) {
        return compare(a, b);
    }

    public static byte toByte(Byte b) {
        return b;
    }

    public static short toShort(Short s) {
        return s;
    }

    public static int toInt(Integer i) {
        return i;
    }

    public static long toLong(Long l) {
        return l;
    }

    public static float toFloat(Float f) {
        return f;
    }

    public static double toDouble(Double d) {
        return d;
    }

    public static char toChar(Character c) {
        return c;
    }

    public static boolean toBoolean(Boolean z) {
        return z;
    }

}
