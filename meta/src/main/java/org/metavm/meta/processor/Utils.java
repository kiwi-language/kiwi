package org.metavm.meta.processor;

import java.util.function.Function;

public class Utils {

    public static <T> String join(Iterable<T> iterable, Function<T, String> mapper) {
        return join(iterable, mapper, ",");
    }

    public static <T> String join(Iterable<T> iterable, Function<T, String> mapper, String delimiter) {
        var it = iterable.iterator();
        if (it.hasNext()) {
            var sb = new StringBuilder();
            sb.append(mapper.apply(it.next()));
            while (it.hasNext()) {
                sb.append(delimiter);
                sb.append(mapper.apply(it.next()));
            }
            return sb.toString();
        } else
            return "";
    }

}
