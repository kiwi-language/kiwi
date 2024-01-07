package tech.metavm.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DescStore {

    private static final DescStore INSTANCE = new DescStore();

    public static EntityDesc get(Class<?> klass) {
        return INSTANCE.getDesc(klass);
    }

    private final Map<Class<?>, EntityDesc> descMap = new ConcurrentHashMap<>();

    public EntityDesc getDesc(Class<?> klass) {
        return descMap.computeIfAbsent(klass, EntityDesc::new);
    }

}
