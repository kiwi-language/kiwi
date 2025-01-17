package org.metavm.util;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public record ChangeList<T>(List<T> inserts, List<T> updates, List<T> deletes) {

    public static <T> ChangeList<T> create(Collection<T> inserts, Collection<T> updates, Collection<T> deletes) {
        return new ChangeList<>(new ArrayList<>(inserts), new ArrayList<>(updates), new ArrayList<>(deletes));
    }

    public static <T> ChangeList<T> empty() {
        return new ChangeList<>(List.of(), List.of(), List.of());
    }

    public static <T> ChangeList<T> inserts(List<T> inserts) {
        return new ChangeList<>(inserts, List.of(), List.of());
    }

    public static <T> ChangeList<T> deletes(List<T> deletes) {
        return new ChangeList<>(List.of(), List.of(), deletes);
    }

    public static <T, K> ChangeList<T> build(Collection<T> beforeList, Collection<T> afterList, Function<T, K> keyMapping) {
        return build(beforeList, afterList, keyMapping, Objects::equals);
    }

    public static <T, K> ChangeList<T> build(Collection<T> beforeList, Collection<T> afterList, Function<T, K> keyMapping, BiPredicate<T, T> equals) {
        if (beforeList == null)
            beforeList = List.of();
        if (afterList == null)
            afterList = List.of();
        List<T> deleted = new ArrayList<>();
        Set<K> newKeys = new HashSet<>();
        afterList.forEach(f -> newKeys.add(keyMapping.apply(f)));
        Map<K, T> beforeMap = new HashMap<>();
        for (T before : beforeList) {
            K oldKey = keyMapping.apply(before);
            beforeMap.put(oldKey, before);
            if (!newKeys.contains(oldKey))
                deleted.add(before);
        }
        List<T> updated = new ArrayList<>();
        List<T> inserted = new ArrayList<>();
        for (T after : afterList) {
            K key = keyMapping.apply(after);
            T before;
            if ((before = beforeMap.get(key)) != null) {
                if (!equals.test(before, after))
                    updated.add(after);
            } else
                inserted.add(after);
        }
        return new ChangeList<>(inserted, updated, deleted);
    }

    public List<T> insertsOrUpdates() {
        return Utils.union(inserts, updates);
    }

    public T any() {
        if (Utils.isNotEmpty(inserts))
            return inserts.getFirst();
        if (Utils.isNotEmpty(updates))
            return updates.getFirst();
        if (Utils.isNotEmpty(deletes))
            return deletes.getFirst();
        throw new InternalException("Empty change list");
    }

    public void apply(
            Consumer<List<T>> insertsConsumer,
            Consumer<List<T>> updatesConsumer,
            Consumer<List<T>> deletesConsumer
    ) {
        if (Utils.isNotEmpty(inserts))
            Utils.doInBatch(inserts, insertsConsumer);
        if (Utils.isNotEmpty(updates))
            Utils.doInBatch(updates, updatesConsumer);
        if (Utils.isNotEmpty(deletes))
            Utils.doInBatch(deletes, deletesConsumer);
    }

    public <R> ChangeList<R> filterAndMap(Predicate<T> filter, Function<T, R> mapper) {
        return new ChangeList<>(
                Utils.filterAndMap(inserts, filter, mapper),
                Utils.filterAndMap(updates, filter, mapper),
                Utils.filterAndMap(deletes, filter, mapper)
        );
    }

    public ChangeList<T> filter(Predicate<T> filter) {
        return new ChangeList<>(
                Utils.filter(inserts, filter),
                Utils.filter(updates, filter),
                Utils.filter(deletes, filter)
        );
    }

    public ChangeList<T> filterNot(Predicate<T> filter) {
        return new ChangeList<>(
                Utils.exclude(inserts, filter),
                Utils.exclude(updates, filter),
                Utils.exclude(deletes, filter)
        );
    }

    public void forEachInsertOrUpdate(Consumer<T> action) {
        inserts.forEach(action);
        updates.forEach(action);
    }
}
