package trycatch;

public class UncheckedExceptionFoo {

    public static int get(int value) {
        int w;
        try {
            if (value < 0 && check(value)) {
                w = 0;
                value++;
            }
        } catch (Throwable ex) {
            return -value;
        }
        return 1;
    }

    private static boolean check(int value) {
        throw new RuntimeException();
    }

    private record Holder(int value) {}

}
