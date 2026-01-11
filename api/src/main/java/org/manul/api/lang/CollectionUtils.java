package org.manul.api.lang;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CollectionUtils {

    @Nullable
    public static <T> T find(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
        for (T t : iterable) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

}
