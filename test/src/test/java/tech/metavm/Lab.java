package tech.metavm;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Lab {

    public static <T> @Nullable T find(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
        for (T t : iterable) {
            if(predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    public static boolean contains(List<String> list, String str) {
        return Lab.<String>find(list, s -> s.equals(str)) != null;
    }

}
