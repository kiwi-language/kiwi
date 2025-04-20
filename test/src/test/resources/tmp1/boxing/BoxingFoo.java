public class BoxingFoo {

    public static <T extends Comparable<T>> int compare(T t1, T t2) {
        return t1.compareTo(t2);
    }

    public static int compareChar(char c1, char c2) {
        return compare(c1, c2);
    }
    public String toString(Object o) {
        return java.util.Objects.toString(o);
    }

    public void printInt(int i) {
        System.out.println(toString(i));
    }

}