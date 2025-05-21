package org.metavm.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.util.Utils;

import java.util.List;
import java.util.function.Function;

public record Page<T>(List<T> items, long total) {

    public static <T> Page<T> empty() {
        return new Page<>(List.of(), 0L);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(
                Utils.map(items, mapper),
                total
        );
    }

}
