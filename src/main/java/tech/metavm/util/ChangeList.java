package tech.metavm.util;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public record ChangeList<T>(List<T> inserts, List<T> updates, List<T> deletes) {

    public static <T> ChangeList<T> empty() {
        return new ChangeList<>(List.of(), List.of(), List.of());
    }

    public static <T> ChangeList<T> inserts(List<T> inserts) {
        return new ChangeList<>(inserts, List.of(), List.of());
    }

    public static <T,K> ChangeList<T> build(List<T> beforeList, List<T> afterList, Function<T, K> keyMapping) {
        return build(beforeList, afterList, keyMapping, Objects::equals);
    }

    public static <T,K> ChangeList<T> build(List<T> beforeList, List<T> afterList, Function<T, K> keyMapping, BiPredicate<T, T> equals) {
        if(beforeList == null) {
            beforeList = List.of();
        }
        if(afterList == null) {
            afterList = List.of();
        }
        List<T> deleted = new ArrayList<>();
        Set<K> newKeys = new HashSet<>();
        afterList.forEach(f -> newKeys.add(keyMapping.apply(f)));
        Map<K, T> beforeMap = new HashMap<>();
        for (T before : beforeList) {
            K oldKey = keyMapping.apply(before);
            beforeMap.put(oldKey, before);
            if(!newKeys.contains(oldKey)) {
                deleted.add(before);
            }
        }
        List<T> updated = new ArrayList<>();
        List<T> inserted = new ArrayList<>();
        for (T after : afterList) {
            K key = keyMapping.apply(after);
            T before;
            if((before = beforeMap.get(key)) != null) {
                if(!equals.test(before, after)) {
                    updated.add(after);
                }
            }
            else {
                inserted.add(after);
            }
        }
        return new ChangeList<>(inserted, updated, deleted);
    }


}
