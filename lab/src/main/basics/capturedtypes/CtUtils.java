package capturedtypes;

import org.metavm.api.lang.Lang;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CtUtils {

    public static <T> T findRequired(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
        var it = iterable;
        Lang.print(iterable);
        T t = find(iterable, predicate);
        if (t != null) {
            return t;
        }
        throw new IllegalArgumentException("Not found");
    }

    public static <T> @Nullable T find(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
        for (T t : iterable) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }
}
