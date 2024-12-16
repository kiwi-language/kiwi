package primitives;

public class PrimitiveCompareFoo {

    public static <T extends Comparable<T>> int compare(T t1, T t2) {
        return t1.compareTo(t2);
    }

    public static int compareString(String s1, String s2) {
        return compare(s1, s2);
    }

    public static int compareChar(char c1, char c2) {
        return compare(c1, c2);
    }

}
