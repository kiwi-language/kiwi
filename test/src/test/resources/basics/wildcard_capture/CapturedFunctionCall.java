package wildcard_capture;

import java.util.Objects;
import java.util.function.Predicate;

public class CapturedFunctionCall<T> {

    public void removeIf(Predicate<? super T> filter) {
        Objects.requireNonNull(filter);
    }

    public static void test() {
        var t = new CapturedFunctionCall<String>();
        t.removeIf(s -> s.startsWith("X"));
    }

}
