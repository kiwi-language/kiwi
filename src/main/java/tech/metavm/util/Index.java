package tech.metavm.util;


import java.util.function.Function;

public class Index<T, K> {
    private final Function<T, K> keyMapper;

    public Index(Function<T, K> keyMapper) {
        this.keyMapper = keyMapper;
    }

    public K mapToKey(T value) {
        return keyMapper.apply(value);
    }
}
