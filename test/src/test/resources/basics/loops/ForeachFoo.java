package loops;

public class ForeachFoo {

    public static long sum(long[] values) {
        var s = 0L;
        for (long value : values) {
            s += value;
        }
        return s;
    }

}
