package org.metavm.util;

import org.metavm.entity.GenericDeclaration;
import org.metavm.object.type.Type;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ParameterizedMap {

    private final WeakHashMap<Object, WeakHashMap<List<? extends Type>, WeakReference<Object>>> map = new WeakHashMap<>();

    public Object get(GenericDeclaration genericDeclaration, List<? extends Type> typeArguments) {
        var subMap = map.get(genericDeclaration);
        if(subMap != null) {
            var ref = subMap.get(typeArguments);
            return ref != null ? ref.get() : null;
        }
        else
            return null;
    }

    public Object put(GenericDeclaration genericDeclaration, List<? extends Type> typeArguments, Object parameterized) {
        var existing = map.computeIfAbsent(genericDeclaration, k -> new WeakHashMap<>())
                .put(typeArguments, new WeakReference<>(parameterized));
        return existing != null ? existing.get() : null;
    }

    public void forEach(Object genericDeclaration, BiConsumer<List<? extends Type>, Object> action) {
        var subMap = map.get(genericDeclaration);
        if(subMap != null)
            subMap.forEach((typeArgs, ref) -> {
                var pKlass = ref.get();
                if(pKlass != null)
                    action.accept(typeArgs, pKlass);
            });
    }

    public void forEach(Consumer<Object> action) {
        map.values().forEach(subMap -> subMap.values().forEach(action));
    }

    public void clear() {
        map.clear();
    }

}
