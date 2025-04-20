package boxing;

public class UnboxingFoo {

    public static long unbox(Long l) {
        return l;
    }

    public static boolean gt(Long l) {
        return l > 0;
    }

    public static long shift(Long v, Integer s) {
        return v >> s;
    }

    public static double sum(Integer i, Long l, Double d) {
        return i + l + d;
    }

}