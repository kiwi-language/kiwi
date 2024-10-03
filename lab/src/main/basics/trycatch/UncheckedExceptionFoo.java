package trycatch;

public class UncheckedExceptionFoo {

    private static Holder holder;

    public static int get(int value) {
        int w;
        try {
            if (value < 0) {
                w = 0;
                value = holder.value;
            }
        } catch (Throwable ex) {
            return -value;
        }
        return 1;
    }

    private static int getValue() {
        throw new RuntimeException();
    }

    private record Holder(int value) {}

}
