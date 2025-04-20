package wildcard_capture;

import org.jetbrains.annotations.NotNull;

public class BoundCaptureFoo {

    public static <T extends Comparable<? super T>> int compare(T t1, T t2) {
        return t1.compareTo(t2);
    }

    public static int test() {
        return compare(new Bar(1), new Bar(2));
    }

    record Bar(int code) implements Comparable<Bar> {

        @Override
            public int compareTo(@NotNull BoundCaptureFoo.Bar o) {
                return Integer.compare(code, o.code);
            }
        }

}
