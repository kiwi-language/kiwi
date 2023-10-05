package tech.metavm.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.intellij.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityPO;
import tech.metavm.entity.Identifiable;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.LinkedList;

public class NncUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN);

    static {
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static void requireLength(Collection<?> collection, int expectedSize) {
        if (collection.size() != expectedSize) {
            throw new InternalException("Expected collection size is " + expectedSize
                    + ", actual collection size is " + collection.size());
        }
    }


    public static Runnable noop() {
        return () -> {};
    }

    public static <K, V> Map<K, V> zip(List<K> keys, List<V> values) {
        Map<K, V> map = new HashMap<>();
        biForEach(keys, values, map::put);
        return map;
    }

    public static String getGetter(String name) {
        return "get" + firstCharToUpperCase(name);
    }

    public static String firstCharToUpperCase(String s) {
        if (s.isEmpty()) {
            return s;
        }
        if (s.length() == 1) {
            return s.toUpperCase();
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }

    public static <T> List<T> concatList(List<T> list, List<T> values) {
        var copy = new ArrayList<>(list);
        copy.addAll(values);
        return copy;
    }

    public static <T> Set<T> diffSet(Set<T> set1, Set<T> set2) {
        Set<T> diff = new HashSet<>(set1);
        diff.removeAll(set2);
        return diff;
    }

    public static <T> Set<T> unionSet(Set<T> set1, Set<T> set2) {
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }

    public static void requireMinimumSize(Object[] array, int minSize) {
        requireMinimumSize(Arrays.asList(array), minSize);
    }

    public static void requireMinimumSize(Collection<?> collection, int minSize) {
        if (collection == null) {
            throw new InternalException("collection is null");
        }
        if (collection.size() < minSize) {
            throw new InternalException("collection has less elements than required. Minimum size: " + minSize);
        }
    }

    public static <T> List<T> prepend(T element, List<? extends T> list) {
        List<T> newList = new ArrayList<>();
        newList.add(element);
        newList.addAll(list);
        return newList;
    }

    public static <T> List<T> append(List<T> list, T element) {
        List<T> newList = new ArrayList<>(list);
        newList.add(element);
        return newList;
    }

    public static byte[] readFromFile(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] bytes = new byte[inputStream.available()];
            int n = inputStream.read(bytes);
            if (n != bytes.length) {
                throw new InternalException("read  bytes " + n + " is not equal to available bytes " + bytes.length + "" +
                        " in file '" + filePath + "'");
            }
            return bytes;
        }
    }

    public static <T> T cast(Class<T> klass, Object object, String message) {
        if (klass.isInstance(object)) {
            return klass.cast(object);
        } else {
            throw new InternalException(message);
        }
    }

    public static String toJSONString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fail to write JSON string, object: " + object, e);
        }
    }

    public static <T> void forEach(Collection<T> collection, Consumer<T> action) {
        if (NncUtils.isEmpty(collection)) {
            return;
        }
        collection.forEach(action);
    }

    public static <T> List<T> concat(List<T> list, T value) {
        List<T> newList = new ArrayList<>(list);
        newList.add(value);
        return newList;
    }

    public static <T> Set<T> intersect(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        result.addAll(set2);
        return result;
    }

    public static <T> T readJSONString(String jsonStr, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fail to read JSON string, JSON string: " + jsonStr, e);
        }
    }


    public static <T> T readJSONString(String jsonStr, TypeReference<T> typeReference) {
        try {
            var reader = OBJECT_MAPPER.reader().forType(typeReference);
            return reader.readValue(jsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fail to read JSON string, JSON string: " + jsonStr, e);
        }
    }

    public static <T> T anyNonNull(T t1, T t2) {
        return t1 != null ? t1 : t2;
    }


    @SuppressWarnings("unused")
    public static <T> T firstNonNull(T t1, T t2) {
        if (t1 == null && t2 == null) {
            throw new IllegalArgumentException("Arguments can't both be null");
        }
        return t1 != null ? t1 : t2;
    }

    public static <T> T firstNonNull(T t1, T t2, T t3) {
        if (t1 == null && t2 == null && t3 == null) {
            throw new IllegalArgumentException("Arguments can't all be null");
        }
        return t1 != null ? t1 : (t2 != null ? t2 : t3);
    }


    public static <T> T firstNonNull(T t1, T t2, T t3, T t4) {
        if (t1 == null && t2 == null && t3 == null && t4 == null) {
            throw new IllegalArgumentException("Arguments can't all be null");
        }
        return t1 != null ? t1 : (t2 != null ? t2 : (t3 != null ? t3 : t4));
    }


    @SuppressWarnings("unused")
    public static <T> Set<T> newSet(Collection<T> coll) {
        return coll != null ? new HashSet<>(coll) : new HashSet<>();
    }

    @SuppressWarnings("unused")
    public static boolean isTrue(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    public static boolean isBlank(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static boolean isEmptyValue(Object value) {
        if (value instanceof String str) {
            return isEmpty(str);
        }
        return value == null;
    }

    public static long maxId(List<? extends Identifiable> list) {
        return maxLong(list, Identifiable::getIdRequired, 0);
    }

    public static <T> long maxLong(List<T> list, Function<T, Long> mapping, long defaultMax) {
        return list.stream().map(mapping).mapToLong(Long::longValue)
                .max().orElse(defaultMax);
    }

    @PassNull
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    @PassNull
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    @PassNonNull
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    @PassNonNull
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static long random() {
        return Math.abs(new Random().nextLong());
    }

    public static List<Integer> splitIntegers(String str) {
        if (isEmpty(str)) {
            return List.of();
        }
        String[] splits = str.split(",");
        List<Integer> results = new ArrayList<>();
        for (String split : splits) {
            try {
                results.add(Integer.parseInt(split.trim()));
            } catch (NumberFormatException e) {
                throw new InternalException("string '" + str + "' is not a valid integer list");
            }
        }
        return results;
    }

    public static <T, R> @Nullable R getFirst(List<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.get(0)) : null;
    }

    public static <T, R> @Nullable R getFirst(LinkedList<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.getFirst()) : null;
    }


    public static <T> @Nullable T getFirst(List<T> list) {
        return getFirst(list, Function.identity());
    }


    public static <T> @Nullable T getFirst(LinkedList<T> list) {
        return getFirst(list, Function.identity());
    }

    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    @PassNonNull
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    @PassNull
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> long count(List<T> list, Predicate<T> filter) {
        return list.stream().filter(filter).count();
    }

    public static <T> List<T> filter(Collection<T> source, Predicate<T> filter) {
        if (source == null) {
            return List.of();
        }
        return source.stream().filter(filter).collect(Collectors.toList());
    }

    public static <T> List<T> fillWith(T value, int times) {
        var list = new ArrayList<T>(times);
        for (int i = 0; i < times; i++) {
            list.add(value);
        }
        return list;
    }

    public static <T> Set<T> filterUnique(Collection<T> source, Predicate<T> filter) {
        if (source == null) {
            return Set.of();
        }
        return source.stream().filter(filter).collect(Collectors.toSet());
    }

    public static <T> List<T> filterAndSort(Collection<T> source, Predicate<T> filter, Comparator<T> comparator) {
        return filterAndSortAndLimit(source, filter, comparator, Long.MAX_VALUE);
    }

    public static <T> List<T> filterAndLimit(Collection<T> source, Predicate<T> filter, long limit) {
        if (source == null) {
            source = List.of();
        }
        return source.stream().filter(filter).limit(limit).collect(Collectors.toList());
    }

    public static <T> List<T> filterAndSortAndLimit(Collection<T> source, Predicate<T> filter, Comparator<T> comparator, long limit) {
        if (source == null) {
            source = List.of();
        }
        return source.stream().filter(filter).sorted(comparator).limit(limit).collect(Collectors.toList());
    }

    public static List<Long> range(long start, long end) {
        return LongStream.range(start, end).boxed().collect(Collectors.toList());
    }

    public static <T> List<T> merge(Collection<? extends T> coll1, Collection<? extends T> coll2) {
        if (coll1 == null) {
            coll1 = List.of();
        }
        if (coll2 == null) {
            coll2 = List.of();
        }
        List<T> merged = new ArrayList<>(coll1);
        merged.addAll(coll2);
        return merged;
    }

    public static <T> Set<T> mergeSets(Set<? extends T> coll1, Set<? extends T> coll2) {
        if (coll1 == null) {
            coll1 = Set.of();
        }
        if (coll2 == null) {
            coll2 = Set.of();
        }
        Set<T> merged = new HashSet<>(coll1);
        merged.addAll(coll2);
        return merged;
    }

    public static <T> Set<T> mergeSets(Set<? extends T> coll1, Set<? extends T> coll2, Set<? extends T> coll3) {
        return mergeSets(mergeSets(coll1, coll2), coll3);
    }

    @SuppressWarnings("unused")
    public static <T> Set<T> mergeUnique(Collection<T> coll1, Collection<T> coll2) {
        Set<T> merged = new HashSet<>(coll1);
        merged.addAll(coll2);
        return merged;
    }

    public static <T> List<T> merge(List<List<T>> collections) {
        List<T> merged = new ArrayList<>();
        for (List<T> coll : collections) {
            merged.addAll(coll);
        }
        return merged;
    }

    public static <T, M, R> List<R> mapAndFilterByType(Collection<T> source, Function<T, M> mapper, Class<R> resultType) {
        return NncUtils.filterByType(
                NncUtils.map(source, mapper),
                resultType
        );
    }

    public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapping) {
        if (source == null) {
            return List.of();
        }
        return source.stream().map(mapping).collect(Collectors.toList());
    }

    public static <T, R> R[] mapArray(T[] source, Function<T, R> mapper, IntFunction<R[]> generator) {
        return Arrays.stream(source).map(mapper).toArray(generator);
    }

    public static <T extends Identifiable> Set<Long> mapToIds(Collection<T> collection) {
        return NncUtils.mapUnique(collection, Identifiable::getIdRequired);
    }

    public static String toBase64(long l) {
        return Base64.getEncoder().encodeToString(toBytes(l));
    }

    public static byte[] toBytes(long l) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (l >> (i << 3));
        }
        return bytes;
    }

    public static <T, R> Map<R, Integer> mapAndCount(List<T> list, Function<T, R> keyMapper) {
        Map<R, Integer> countMap = new HashMap<>();
        for (T item : list) {
            R key = keyMapper.apply(item);
            countMap.compute(key, (k, old) -> old != null ? old + 1 : 1);
        }
        return countMap;
    }

    public static <T, M, R> List<R> map(Collection<T> source, Function<T, M> mapping1, Function<M, R> mapping2) {
        return map(map(source, mapping1), mapping2);
    }

    @SuppressWarnings("unused")
    public static <T, K, V> Map<K, List<V>> groupBy(List<T> list, Function<T, K> keyMapping, Function<T, V> valueMapping) {
        Map<K, List<V>> result = new HashMap<>();
        for (T t : list) {
            K k = keyMapping.apply(t);
            result.computeIfAbsent(k, k1 -> new ArrayList<>()).add(valueMapping.apply(t));
        }
        return result;
    }

    public static <T, R> Set<R> mapUnique(Collection<T> source, Function<T, R> mapping) {
        if (source == null) {
            return Set.of();
        }
        return source.stream().map(mapping).collect(Collectors.toSet());
    }

    public static <T, R> List<R> map(T[] source, Function<T, R> mapping) {
        if (source == null) {
            return List.of();
        }
        return Arrays.stream(source).map(mapping).collect(Collectors.toList());
    }

    public static int sum(int[] values) {
        int sum = 0;
        for (int num : values) {
            sum += num;
        }
        return sum;
    }

    public static <T> List<T> sort(Collection<T> list, Comparator<T> comparator) {
        return sortAndMap(list, comparator, Function.identity());
    }

    public static <T extends Identifiable> List<T> sortById(List<T> list) {
        return sort(list, Comparator.comparingLong(Identifiable::getIdRequired));
    }

    public static <T> List<T> sortByInt(Collection<T> list, ToIntFunction<T> intMapper) {
        return sortAndMap(list, Comparator.comparingInt(intMapper), Function.identity());
    }

    public static <T, R> List<R> sortByIntAndMap(Collection<T> list, ToIntFunction<T> intFunc, Function<T, R> mapper) {
        return sortAndMap(list, Comparator.comparingInt(intFunc), mapper);
    }

    public static <T, R> List<R> sortAndMap(Collection<T> list, Comparator<T> comparator, Function<T, R> mapper) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .sorted(comparator)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> mapAndSort(Collection<T> list, Function<T, R> mapper, Comparator<R> comparator) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .map(mapper)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public static <K, V> int binarySearch(List<V> list, K key, ToIntBiFunction<V, K> compare) {
        int l = 0, h = list.size();
        while (l != h) {
            int m = l + h >> 1;
            V v = list.get(m);
            int c = compare.applyAsInt(v, key);
            if (c == 0) {
                return m;
            } else if (c < 0) {
                l = m + 1;
            } else {
                h = m;
            }
        }
        return -l - 1;
    }

    public static <T, R> @Nullable R mapFirst(List<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.get(0)) : null;
    }

    public static <T, R> void biForEach(T[] list1, R[] list2, BiConsumer<T, R> action) {
        biForEach(List.of(list1), List.of(list2), action);
    }

    public static <T, R> void biForEach(Collection<T> list1, Collection<R> list2, BiConsumer<T, R> action) {
        if (list1.size() != list2.size()) {
            throw new RuntimeException("Both lists must have the same size");
        }
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            action.accept(it1.next(), it2.next());
        }
    }

    public static <T, R> void biForEachWithIndex(Collection<T> list1, Collection<R> list2, TriConsumer<T, R, Integer> action) {
        if (list1.size() != list2.size()) {
            throw new RuntimeException("Both lists must have the same size");
        }
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();
        int index = 0;
        while (it1.hasNext() && it2.hasNext()) {
            action.accept(it1.next(), it2.next(), index++);
        }
    }

    public static <T1, T2, R> List<R> biMap(List<T1> list1, List<T2> list2, BiFunction<T1, T2, R> mapper) {
        if (list1.size() != list2.size()) {
            throw new RuntimeException("Both lists must have the same size");
        }
        Iterator<T1> it1 = list1.iterator();
        Iterator<T2> it2 = list2.iterator();

        List<R> result = new ArrayList<>();
        while (it1.hasNext() && it2.hasNext()) {
            result.add(mapper.apply(it1.next(), it2.next()));
        }
        return result;
    }

    public static <T, R> List<R> filterAndMap(Collection<T> source, Predicate<T> filter, Function<T, R> mapping) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .filter(filter)
                .map(mapping)
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> filterByType(Collection<T> source, Class<R> type) {
        return filterAndMap(source, type::isInstance, type::cast);
    }

    @SuppressWarnings("unused")
    public static <T, R> Set<R> filterAndMapUnique(Collection<T> source, Predicate<T> filter, Function<T, R> mapping) {
        if (source == null) {
            return Set.of();
        }
        return source.stream()
                .filter(filter)
                .map(mapping)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unused")
    public static <T, R> List<R> mapNonNull(Collection<T> source, Function<T, R> mapper) {
        return mapAndFilter(
                source,
                mapper,
                Objects::nonNull
        );
    }

    public static <T, R> Set<R> mapNonNullUnique(Collection<T> source, Function<T, R> mapper) {
        return mapAndFilterUnique(
                source,
                mapper,
                Objects::nonNull
        );
    }

    public static <T> List<T> deduplicateAndSort(List<T> list, Comparator<T> comparator) {
        if (list == null) {
            list = List.of();
        }
        list = new ArrayList<>(new HashSet<>(list));
        list.sort(comparator);
        return list;
    }

    public static <T> @Nullable T find(Collection<T> list, Predicate<T> filter) {
        if (list == null) {
            return null;
        }
        return list.stream().filter(filter).findAny().orElse(null);
    }

    public static <T> boolean exists(Collection<T> list, Predicate<T> filter) {
        return find(list, filter) != null;
    }

    public static <T> @Nullable T find(T[] array, Predicate<T> filter) {
        return Arrays.stream(array).filter(filter).findAny().orElse(null);
    }

    @SuppressWarnings("unused")
    public static <R> List<R> splitAndMap(String str, Function<String, R> mapping) {
        return splitAndMap(str, ",", mapping);
    }

    public static <R> List<R> splitAndMap(String str, String delimiter, Function<String, R> mapping) {
        return Arrays.stream(str.split(delimiter))
                .map(mapping)
                .collect(Collectors.toList());
    }

    public static <T> void splitAndForEach(Collection<T> collection, Predicate<T> test, Consumer<List<T>> action1, Consumer<List<T>> action2) {
        List<T> list1 = filter(collection, test);
        List<T> list2 = filterNot(collection, test);
        if (isNotEmpty(list1)) {
            action1.accept(list1);
        }
        if (isNotEmpty(list2)) {
            action2.accept(list2);
        }
    }

    public static <T> T findRequired(T[] array, Predicate<T> filter) {
        return Arrays.stream(array).filter(filter).findAny()
                .orElseThrow(InternalException::new);
    }

    public static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        int i = 0;
        for (T t : list) {
            if (predicate.test(t)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static <T> T findRequired(Collection<T> collection, Predicate<T> filter) {
        return findRequired(collection, filter, NullPointerException::new);
    }

    public static <T> T findRequired(Collection<T> collection, Predicate<T> filter, String message) {
        return findRequired(collection, filter, () -> new InternalException(message));
    }

    public static <T> T findRequired(Collection<T> collection, Predicate<T> filter,
                                     Supplier<RuntimeException> exceptionSupplier) {
        if (collection == null) {
            throw exceptionSupplier.get();
        }
        return collection.stream().filter(filter).findAny()
                .orElseThrow(exceptionSupplier);
    }

    public static <T extends Identifiable> T findById(Collection<T> collection, long id) {
        return findRequired(collection, item -> Objects.equals(item.getId(), id));
    }

    @SuppressWarnings("unused")
    public static <T> void invokeIfNotEmpty(Collection<T> collection, Consumer<Collection<T>> action) {
        if (isNotEmpty(collection)) {
            action.accept(collection);
        }
    }

    public static <T> T filterOneRequired(Collection<T> list, Predicate<T> filter, String errorMessage) {
        return list.stream().filter(filter).findAny().orElseThrow(() -> new RuntimeException(errorMessage));
    }

    @Nullable
    public static <T, R> R filterOneAndMap(Collection<T> list, Predicate<T> filter, Function<T, R> mapping) {
        return list.stream().filter(filter).map(mapping).findAny().orElse(null);
    }

    @SuppressWarnings("unused")
    public static <T> String filterAndJoin(Collection<T> list, Predicate<T> filter, Function<T, String> mapping) {
        return join(list, filter, mapping, ",");
    }

    public static <T> String join(Collection<T> list, Function<T, String> mapping) {
        return join(list, mapping, ",");
    }


    public static <T> String join(T[] array, Function<T, String> mapping) {
        return join(Arrays.asList(array), mapping);
    }

    public static <T> String join(Collection<T> list, Function<T, String> mapping, String delimiter) {
        return join(list, t -> true, mapping, delimiter);
    }

    public static <T> String join(Collection<T> list, Predicate<T> filter, Function<T, String> mapping, String delimiter) {
        return list.stream().filter(filter).map(mapping).collect(Collectors.joining(delimiter));
    }

    public static String join(Collection<String> strList) {
        return join(strList, ",");
    }

    public static String join(Collection<String> strList, String delimiter) {
        return String.join(delimiter, strList);
    }

    public static String joinWithDot(Collection<String> strList) {
        return String.join(".", strList);
    }


    public static <K, T> Map<K, T> toMap(Collection<T> list, Function<T, K> keyMapper) {
        return toMap(list, keyMapper, Function.identity());
    }

    public static <K, T> Map<K, T> toMap(T[] array, Function<T, K> keyMapper) {
        if (array == null) {
            return Map.of();
        }
        return toMap(Arrays.asList(array), keyMapper, Function.identity());
    }

    public static <T extends Entity> Map<Long, T> toEntityMap(Collection<T> coll) {
        return toMap(coll, Entity::getIdRequired);
    }

    public static <K, V> List<Pair<V>> buildPairsForMap(Map<? extends K, ? extends V> map1,
                                                        Map<? extends K, ? extends V> map2) {
        List<Pair<V>> pairs = new ArrayList<>();
        map1.forEach((k1, v1) ->
                pairs.add(new Pair<>(v1, map2.get(k1)))
        );

        map2.forEach((k2, v2) -> {
            if (!map1.containsKey(k2)) {
                pairs.add(new Pair<>(null, v2));
            }
        });
        return pairs;
    }

    public static <K, T, R> Map<K, R> toMap(Collection<T> list, Function<T, ? extends K> keyMapper, Function<T, ? extends R> valueMapper) {
        if (list == null) {
            return new HashMap<>();
        }
        Map<K, R> map = new HashMap<>();
        for (T item : list) {
            map.put(keyMapper.apply(item), valueMapper.apply(item));
        }
        return map;
    }

    public static <K, T> IdentityHashMap<K, T> toIdentityMap(Collection<T> list, Function<T, K> keyMapping) {
        if (list == null) {
            return new IdentityHashMap<>();
        }
        IdentityHashMap<K, T> map = new IdentityHashMap<>();
        for (T item : list) {
            map.put(keyMapping.apply(item), item);
        }
        return map;
    }

    public static <K, T> Map<K, List<T>> toMultiMap(Collection<T> list, Function<T, K> keyMapping) {
        return toMultiMap(list, keyMapping, Function.identity());
    }

    public static <K, T, R> Map<K, List<R>> toMultiMap(Collection<T> list, Function<T, K> keyMapper, Function<T, R> valueMapper) {
        if (list == null) {
            return new HashMap<>();
        }
        Map<K, List<R>> map = new HashMap<>();
        for (T item : list) {
            map.computeIfAbsent(keyMapper.apply(item), k -> new ArrayList<>()).add(valueMapper.apply(item));
        }
        return map;
    }

    public static <T, R> List<R> mapAndFilter(Collection<T> source, Function<T, R> mapping, Predicate<R> filter) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(mapping)
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static <T, R> Set<R> mapAndFilterUnique(Collection<T> source, Function<T, R> mapping, Predicate<R> filter) {
        if (source == null) {
            return Set.of();
        }
        return source.stream()
                .map(mapping)
                .filter(filter)
                .collect(Collectors.toSet());
    }

    public static List<Pair<EntityPO>> buildEntityPairs(Collection<EntityPO> list1, Collection<EntityPO> list2) {
        return buildPairs(list1, list2, EntityPO::getId);
    }

    public static <T, R> Map<T, R> buildMap(List<T> list1, Collection<R> list2) {
        requireTrue(list1.size() == list2.size(), "both list must have same size");
        Map<T, R> map = new LinkedHashMap<>();
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            map.put(it1.next(), it2.next());
        }
        return map;
    }

    public static <T> List<Pair<@org.jetbrains.annotations.Nullable T>> buildPairs(Collection<? extends T> coll1, Collection<? extends T> coll2) {
        List<Pair<@org.jetbrains.annotations.Nullable T>> pairs = new ArrayList<>();
        Iterator<? extends T> it1 = coll1.iterator(), it2 = coll2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            pairs.add(new Pair<>(it1.next(), it2.next()));
        }
        while (it1.hasNext()) {
            pairs.add(new Pair<>(it1.next(), null));
        }
        while (it2.hasNext()) {
            pairs.add(new Pair<>(null, it2.next()));
        }
        return pairs;
    }

    public static <T, K> List<Pair<T>> buildPairs(Collection<T> list1, Collection<T> list2, Function<T, K> keyMapping) {
        Map<K, Queue<T>> firstMap = new HashMap<>();
        for (T t : list1) {
            firstMap.computeIfAbsent(keyMapping.apply(t), (k) -> new LinkedList<>())
                    .offer(t);
        }
        List<Pair<T>> result = new ArrayList<>();
        for (T second : list2) {
            Queue<T> firsts = firstMap.get(keyMapping.apply(second));
            if (isEmpty(firsts)) {
                result.add(new Pair<>(null, second));
            } else {
                result.add(new Pair<>(firsts.poll(), second));
            }
        }

        for (Queue<T> firsts : firstMap.values()) {
            while (!firsts.isEmpty()) {
                result.add(new Pair<>(firsts.poll(), null));
            }
        }

        return result;
    }

    @SuppressWarnings("unused")
    public static <T> List<T> deduplicate(Collection<T> list) {
        return new ArrayList<>(new HashSet<>(list));
    }

    public static <T, R> List<R> flatMap(Collection<T> list, Function<T, Collection<R>> mapping) {
        return flatMapAndFilter(list, mapping, e -> true);
    }

    public static <T, R> List<R> flatMapAndFilter(Collection<T> list, Function<T, Collection<R>> mapping, Predicate<R> filter) {
        if (NncUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .flatMap(e -> {
                    Collection<R> coll = mapping.apply(e);
                    return coll != null ? coll.stream() : Stream.empty();
                })
                .filter(filter)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public static <T, R> List<R> flatMap(T[] array, Function<T, List<R>> mapping) {
        List<R> result = new ArrayList<>();
        for (T item : array) {
            result.addAll(mapping.apply(item));
        }
        return result;
    }

    public static String convertToString(Object object) {
        return object == null ? "" : object.toString();
    }

    public static <T> List<T> filterNot(Collection<T> list, Predicate<T> filter) {
        if (list == null) {
            return List.of();
        }
        return list.stream().filter(item -> !filter.test(item)).collect(Collectors.toList());

    }

    @Nullable
    public static <T, R> R get(@Nullable T t, Function<T, R> mapping) {
        return t != null ? mapping.apply(t) : null;
    }

    @Nullable
    public static <T, R> R get(@Nullable T t, Function<T, R> mapping, Supplier<R> defaultSupplier) {
        return t != null ? mapping.apply(t) : defaultSupplier.get();
    }

    public static <T, R> R getOrElse(@Nullable T t, Function<T, R> mapping, R defaultValue) {
        return t != null ? mapping.apply(t) : defaultValue;
    }

    public static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    public static <T> boolean listEquals(List<T> list1, List<T> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        Iterator<T> it1 = list1.iterator(), it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }
        return true;
    }

    public static <T> T orElse(T t, Supplier<T> elseSupplier) {
        return mapOrElse(t, Function.identity(), elseSupplier);
    }

    public static <T, R> R mapOrElse(T t, Function<T, R> mapping, Supplier<R> elseSupplier) {
        return t != null ? mapping.apply(t) : elseSupplier.get();
    }

    @Nullable
    public static <T, M, R> R get(@Nullable T t, Function<T, M> mapping1, Function<M, R> mapping2) {
        return get(get(t, mapping1), mapping2);
    }

    public static <T> void invokeIfNotNull(@Nullable T t, Consumer<T> action) {
        if (t != null) {
            action.accept(t);
        }
    }

    public static <T> void invokeIf(T t, Predicate<T> test, Consumer<T> action) {
        if (test.test(t)) {
            action.accept(t);
        }
    }

    public static <T> void invokeIfNot(T t, Predicate<T> test, Consumer<T> action) {
        if (!test.test(t)) {
            action.accept(t);
        }
    }

    public static <T, R> void getAndInvoke(T t, Function<T, R> mapper, Consumer<R> action) {
        invokeIfNotNull(get(t, mapper), action);
    }

    public static <T> T requireNonNull(@Nullable T value) {
        return requireNonNull(value, "参数不能为空");
    }

    @Nullable
    public static <T> T requireNull(@Nullable T value) {
        return requireNull(value, "Value必须为空");
    }

    @Nullable
    public static <T> T requireNull(@Nullable T value, String message) {
        return requireNull(value, () -> new InternalException(message));
    }

    @Nullable
    public static <T> T requireNull(@Nullable T value, Supplier<RuntimeException> exceptionSupplier) {
        if (value != null) {
            throw exceptionSupplier.get();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static void requirePositive(int value) {
        requireTrue(value > 0, () -> new InternalException("参数必须大于0"));
    }

    @SuppressWarnings("unused")
    public static void requireLessThan(int value, int max) {
        requireTrue(value < max, () -> new InternalException("参数必须小于" + max));
    }

    public static void requireRangeInclusively(int value, int min, int max) {
        requireTrue(value >= min && value <= max, () -> new InternalException("参数必须在区间[" + min + "," + max + "]中"));
    }

    public static <T> T requireNonNull(@Nullable T value, String message) {
        return requireNonNull(value, () -> new InternalException(message));
    }

    public static <T> boolean equalsIgnoreOrder(Collection<T> coll1, Collection<T> coll2) {
        if(coll1.size() != coll2.size()) {
            return false;
        }
        return new HashSet<>(coll1).equals(new HashSet<>(coll2));
    }

    public static <T> T requireNonNull(@Nullable T value, Supplier<RuntimeException> exceptionSupplier) {
        if (value == null) {
            throw exceptionSupplier.get();
        }
        return value;
    }

    public static void requireFalse(boolean value) {
        requireTrue(!value, "表达式必须为false");
    }

    public static void requireFalse(boolean value, Supplier<? extends RuntimeException> exceptionSupplier) {
        requireTrue(!value, exceptionSupplier);
    }

    public static void requireFalse(boolean value, String message) {
        requireTrue(!value, message);
    }

    public static void requireTrue(boolean value) {
        requireTrue(value, "Value must be true");
    }

    public static void requireTrue(boolean value, String message) {
        requireTrue(value, () -> new InternalException(message));
    }

    public static void requireTrue(boolean value, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (!value) {
            throw exceptionSupplier.get();
        }
    }

    public static void requireEquals(Object first, Object second) {
        requireEquals(first, second, second + " is not equal to the expected value " + first);
    }

    public static void requireEquals(Object first, Object second, String message) {
        requireEquals(first, second, () -> new InternalException(message));
    }

    public static void requireEquals(Object first, Object second, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (!Objects.equals(first, second)) {
            throw exceptionSupplier.get();
        }
    }

    @NotNull
    public static <T> Collection<T> requireNotEmpty(Collection<T> collection) {
        return requireNotEmpty(collection, "集合不能为空");
    }

    public static <T> Collection<T> requireNotEmpty(Collection<T> collection, String message) {
        if (isEmpty(collection)) {
            throw BusinessException.invalidParams(message);
        }
        return collection;
    }


    @NotNull
    public static <T> List<T> requireNotEmpty(List<T> collection) {
        return requireNotEmpty(collection, "集合不能为空");
    }

    public static <T> List<T> requireNotEmpty(List<T> collection, String message) {
        if (isEmpty(collection)) {
            throw BusinessException.invalidParams(message);
        }
        return collection;
    }

    @SuppressWarnings("unused")
    public static <E> void addIfNotNull(List<E> list, E item) {
        if (item != null) {
            list.add(item);
        }
    }

    public static String escape(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\\') {
                buf.append("\\\\");
            } else if (str.charAt(i) == '\"') {
                buf.append("\"");
            } else {
                buf.append(str.charAt(i));
            }
        }
        return buf.toString();
    }

    public static <T> List<T> merge(List<T> list, T... elements) {
        var merged = new ArrayList<>(list);
        merged.addAll(Arrays.asList(elements));
        return merged;
    }

    public static <T> void addRepeatedly(List<T> list, T element, int times) {
        for (int i = 0; i < times; i++) {
            list.add(element);
        }
    }

    public static <T, R> List<R> splitAndMerge(Collection<T> source,
                                               Predicate<T> test,
                                               Function<List<T>, List<R>> loader1,
                                               Function<List<T>, List<R>> loader2) {
        List<T> list1 = filter(source, test);
        List<T> list2 = filterNot(source, test);
        List<R> result1 = isNotEmpty(list1) ? loader1.apply(list1) : List.of();
        List<R> result2 = isNotEmpty(list2) ? loader2.apply(list2) : List.of();
        return merge(result1, result2);
    }

    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return false;
        }
        return collection.stream().anyMatch(predicate);
    }

    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return true;
        }
        return collection.stream().allMatch(predicate);
    }

    public static <K, V> Map<K, V> cloneMap(Map<K, V> map, Function<V, V> valueClone) {
        var clone = new HashMap<K, V>();
        map.forEach((k, v) -> clone.put(k, valueClone.apply(v)));
        return clone;
    }

    public static boolean allTrue(List<Boolean> booleans) {
        return NncUtils.allMatch(booleans, b -> b);
    }

    public static <T, R> Set<R> flatMapUnique(Collection<T> list, Function<T, ? extends Collection<R>> mapper) {
        if (NncUtils.isEmpty(list)) {
            return Set.of();
        }
        return list.stream()
                .flatMap(e -> {
                    Collection<R> coll = mapper.apply(e);
                    return coll != null ? coll.stream() : Stream.empty();
                })
                .collect(Collectors.toSet());
    }
}
