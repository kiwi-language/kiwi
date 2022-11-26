package tech.metavm.dto;

import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.function.Function;

public record Page<T>(List<T> data, long total) {

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(
                NncUtils.map(data, mapper),
                total
        );
    }

}
