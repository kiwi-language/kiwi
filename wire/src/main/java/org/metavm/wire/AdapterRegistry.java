package org.metavm.wire;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

public class AdapterRegistry {

    public static final AdapterRegistry instance = new AdapterRegistry();

    private final Map<Class<?>, WireAdapter<?>> clazz2adapter = new HashMap<>();
    private final Map<Integer, WireAdapter<?>> tag2adapter = new HashMap<>();
    @Getter
    private final WireAdapter<Object> objectAdapter;

    private AdapterRegistry() {
        var adapters = ServiceLoader.load(WireAdapter.class, AdapterRegistry.class.getClassLoader());
        for (var adapter : adapters) {
            for (var supportedType : adapter.getSupportedTypes()) {
                clazz2adapter.put((Class<?>) supportedType, adapter);
            }
            var tag = adapter.getTag();
            if (tag != -1)
                tag2adapter.put(tag, adapter);
        }
        //noinspection unchecked
        objectAdapter = (WireAdapter<Object>) Objects.requireNonNull(clazz2adapter.get(Object.class));
        for (var adapter : adapters) {
            adapter.init(this);
        }
    }

    public <T> WireAdapter<T> getAdapter(Class<T> clazz) {
        var adapter = clazz2adapter.get(clazz);
        if (adapter == null)
            //noinspection unchecked
            return (WireAdapter<T>) Objects.requireNonNull(objectAdapter);
        else {
            //noinspection unchecked
            return (WireAdapter<T>) Objects.requireNonNull(adapter, () -> "No adapter found for class: " + clazz.getName());
        }
    }

    public WireAdapter<?> getAdapter(int tag) {
        return Objects.requireNonNull(tag2adapter.get(tag), () -> "No adapter found for tag: " + tag);
    }

}
