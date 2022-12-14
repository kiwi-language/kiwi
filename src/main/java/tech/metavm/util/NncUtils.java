package tech.metavm.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
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

public class NncUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN);

    public static void requireMinimumSize(Collection<?> collection, int minSize) {
        if(collection == null) {
           throw new InternalException("collection is null");
        }
        if(collection.size() < minSize) {
            throw new InternalException("collection has less elements than required. Minimum size: " + minSize );
        }
    }

    public static byte[] readFromFile(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] bytes = new byte[inputStream.available()];
            int n = inputStream.read(bytes);
            if(n != bytes.length) {
                throw new InternalException("read  bytes " + n + " is not equal to available bytes " + bytes.length + "" +
                        " in file '" + filePath + "'");
            }
            return bytes;
        }
    }

    public static <T> T cast(Class<T> klass, Object object, String message) {
        if(klass.isInstance(object)) {
            return klass.cast(object);
        }
        else {
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
        if(NncUtils.isEmpty(collection)) {
            return;
        }
        collection.forEach(action);
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
            return OBJECT_MAPPER.readValue(jsonStr, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fail to read JSON string, JSON string: " + jsonStr, e);
        }
    }

    public static <T> T anyNonNull(T t1, T t2) {
        return t1 != null ? t1 : t2;
    }


    @SuppressWarnings("unused")
    public static <T> T firstNonNull(T t1, T t2) {
        if(t1 == null && t2 == null) {
            throw new IllegalArgumentException("Arguments can't both be null");
        }
        return t1 != null ? t1 : t2;
    }

    public static <T> T firstNonNull(T t1, T t2, T t3) {
        if(t1 == null && t2 == null && t3 == null) {
            throw new IllegalArgumentException("Arguments can't all be null");
        }
        return t1 != null ? t1 : (t2 != null ? t2 : t3);
    }


    public static <T> T firstNonNull(T t1, T t2, T t3, T t4) {
        if(t1 == null && t2 == null && t3 == null && t4 == null) {
            throw new IllegalArgumentException("Arguments can't all be null");
        }
        return t1 != null ? t1 : (t2 != null ? t2 : (t3 != null ? t3 : t4 ));
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
        if(value instanceof String str) {
            return isEmpty(str);
        }
        return value == null;
    }

    public static long maxId(List<? extends Identifiable> list) {
        return maxLong(list, Identifiable::getId, 0);
    }

    public static <T> long maxLong(List<T> list, Function<T, Long> mapping, long defaultMax) {
        return list.stream().map(mapping).mapToLong(Long::longValue)
                .max().orElse(defaultMax);
    }

    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static long random() {
        return new Random().nextLong();
    }

    public static List<Integer> splitIntegers(String str) {
        if(isEmpty(str)) {
            return List.of();
        }
        String[] splits = str.split(",");
        List<Integer> results = new ArrayList<>();
        for (String split : splits) {
            try {
                results.add(Integer.parseInt(split.trim()));
            }
            catch (NumberFormatException e) {
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

    public static boolean isNotEmpty(Map<?,?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> long count(List<T> list, Predicate<T> filter) {
        return list.stream().filter(filter).count();
    }

    public static <T> List<T> filter(Collection<T> source, Predicate<T> filter) {
        if(source == null) {
            return List.of();
        }
        return source.stream().filter(filter).collect(Collectors.toList());
    }

    public static List<Long> range(long start, long end) {
        return LongStream.range(start, end).boxed().collect(Collectors.toList());
    }

    public static <T> List<T> merge(Collection<T> coll1, Collection<T> coll2) {
        if(coll1 == null) {
            coll1 = List.of();
        }
        if(coll2 == null) {
            coll2 = List.of();
        }
        List<T> merged = new ArrayList<>(coll1);
        merged.addAll(coll2);
        return merged;
    }

    public static <T> Set<T> mergeSets(Set<? extends T> coll1, Set<? extends T> coll2, Set<? extends T> coll3) {
        if(coll1 == null) {
            coll1 = Set.of();
        }
        if(coll2 == null) {
            coll2 = Set.of();
        }
        if(coll3 == null) {
            coll3 = Set.of();
        }
        Set<T> merged = new HashSet<>(coll1);
        merged.addAll(coll2);
        merged.addAll(coll3);
        return merged;
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
        if(source == null) {
            return List.of();
        }
        return source.stream().map(mapping).collect(Collectors.toList());
    }

    public static <T extends Identifiable> Set<Long> mapToIds(Collection<T> collection) {
        return NncUtils.mapUnique(collection, Identifiable::getId);
    }

    public static  <T, R> Map<R, Integer> mapAndCount(List<T> list, Function<T, R> keyMapper) {
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
        if(source == null) {
            return Set.of();
        }
        return source.stream().map(mapping).collect(Collectors.toSet());
    }

    public static <T, R> List<R> map(T[] source, Function<T, R> mapping) {
        if(source == null) {
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
        return sort(list, Comparator.comparingLong(Identifiable::getId));
    }

    public static <T> List<T> sortByInt(Collection<T> list, ToIntFunction<T> intMapper) {
        return sortAndMap(list, Comparator.comparingInt(intMapper), Function.identity());
    }

    public static <T, R> List<R> sortByIntAndMap(Collection<T> list, ToIntFunction<T> intFunc, Function<T, R> mapper) {
        return sortAndMap(list, Comparator.comparingInt(intFunc), mapper);
    }

    public static <T, R> List<R> sortAndMap(Collection<T> list, Comparator<T> comparator, Function<T, R> mapper) {
        if(list == null) {
            return List.of();
        }
        return list.stream()
                .sorted(comparator)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> mapAndSort(Collection<T> list, Function<T, R> mapper, Comparator<R> comparator) {
        if(list == null) {
            return List.of();
        }
        return list.stream()
                .map(mapper)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public static <T, R> @Nullable R mapFirst(List<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.get(0)) : null;
    }

    public static <T,R> void biForEach(List<T> list1, List<R> list2, BiConsumer<T,R> action) {
        if(list1.size() != list2.size()) {
            throw new RuntimeException("Both lists must have the same size");
        }
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();

        while(it1.hasNext() && it2.hasNext()) {
            action.accept(it1.next(), it2.next());
        }
    }

    public static <T, R> List<R> filterAndMap(Collection<T> source, Predicate<T> filter, Function<T, R> mapping) {
        if(source == null) {
            return List.of();
        }
        return source.stream()
                .filter(filter)
                .map(mapping)
                .collect(Collectors.toList());
    }

    public static <T,R> List<R> filterByType(Collection<T> source, Class<R> type) {
        return filterAndMap(source, type::isInstance, type::cast);
    }

    @SuppressWarnings("unused")
    public static <T, R> Set<R> filterAndMapUnique(Collection<T> source, Predicate<T> filter, Function<T, R> mapping) {
        if(source == null) {
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

    public static <T> @Nullable T find(Collection<T> list, Predicate<T> filter) {
        return list.stream().filter(filter).findAny().orElse(null);
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
        if(isNotEmpty(list1)) {
            action1.accept(list1);
        }
        if(isNotEmpty(list2)) {
            action2.accept(list2);
        }
    }

    public static <T> T findRequired(T[] array, Predicate<T> filter) {
        return Arrays.stream(array).filter(filter).findAny()
                .orElseThrow(InternalException::new);
    }

    public static <T> T findRequired(Collection<T> collection, Predicate<T> filter) {
        return findRequired(collection, filter, NullPointerException::new);
    }

    public static <T> T findRequired(Collection<T> collection, Predicate<T> filter, String message) {
        return findRequired(collection, filter, () -> new InternalException(message));
    }

    public static <T> T findRequired(Collection<T> collection, Predicate<T> filter,
                                     Supplier<RuntimeException> exceptionSupplier) {
        if(collection == null) {
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
        if(isNotEmpty(collection)) {
            action.accept(collection);
        }
    }

    public static <T> T filterOneRequired(Collection<T> list, Predicate<T> filter, String errorMessage) {
        return list.stream().filter(filter).findAny().orElseThrow(() -> new RuntimeException(errorMessage));
    }

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

    public static <K, T> Map<K, T> toMap(Collection<T> list, Function<T, K> keyMapper) {
        return toMap(list, keyMapper, Function.identity());
    }

    public static <K, T> Map<K, T> toMap(T[] array, Function<T, K> keyMapper) {
        if(array == null) {
            return Map.of();
        }
        return toMap(Arrays.asList(array), keyMapper, Function.identity());
    }

    public static <T extends Entity> Map<Long, T> toEntityMap(Collection<T> coll) {
        return toMap(coll, Entity::getId);
    }

    public static <K, V> List<Pair<V>> buildPairsForMap(Map<? extends K, ? extends V> map1,
                                                        Map<? extends K, ? extends V> map2) {
        List<Pair<V>> pairs = new ArrayList<>();
        map1.forEach((k1,v1) ->
            pairs.add(new Pair<>(v1, map2.get(k1)))
        );

        map2.forEach((k2,v2)->{
            if(!map1.containsKey(k2)) {
                pairs.add(new Pair<>(null, v2));
            }
        });
        return pairs;
    }

    public static <K, T, R> Map<K, R> toMap(Collection<T> list, Function<T, K> keyMapper, Function<T, R> valueMapper) {
        if(list == null) {
            return new HashMap<>();
        }
        Map<K, R> map = new HashMap<>();
        for (T item : list) {
            map.put(keyMapper.apply(item), valueMapper.apply(item));
        }
        return map;
    }

    public static <K, T> IdentityHashMap<K, T> toIdentityMap(Collection<T> list, Function<T, K> keyMapping) {
        if(list == null) {
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
        if(list == null) {
            return new HashMap<>();
        }
        Map<K, List<R>> map = new HashMap<>();
        for (T item : list) {
            map.computeIfAbsent(keyMapper.apply(item), k->new ArrayList<>()).add(valueMapper.apply(item));
        }
        return map;
    }

    public static <T, R> List<R> mapAndFilter(Collection<T> source, Function<T, R> mapping, Predicate<R> filter) {
        if(source == null) {
            return List.of();
        }
        return source.stream()
                .map(mapping)
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static <T, R> Set<R> mapAndFilterUnique(Collection<T> source, Function<T, R> mapping, Predicate<R> filter) {
        if(source == null) {
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

    public static <T,R> Map<T,R> buildMap(List<T> list1, Collection<R> list2) {
        requireTrue(list1.size() == list2.size(), "both list must have same size");
        Map<T, R> map = new LinkedHashMap<>();
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            map.put(it1.next(), it2.next());
        }
        return map;
    }

    public static <T> List<Pair<T>> buildPairs(Collection<? extends T> coll1, Collection<? extends T> coll2) {
        List<Pair<T>> pairs = new ArrayList<>();
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
            firstMap.computeIfAbsent(keyMapping.apply(t), (k)->new LinkedList<>())
                    .offer(t);
        }
        List<Pair<T>> result = new ArrayList<>();
        for (T second : list2) {
           Queue<T> firsts = firstMap.get(keyMapping.apply(second));
           if(isEmpty(firsts)) {
               result.add(new Pair<>(null, second));
           }
           else {
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

    public static <T,R> List<R> flatMap(Collection<T> list, Function<T, Collection<R>> mapping) {
        return flatMapAndFilter(list, mapping, e -> true);
    }

    public static <T,R> List<R> flatMapAndFilter(Collection<T> list, Function<T, Collection<R>> mapping, Predicate<R> filter) {
        if(NncUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .flatMap(e ->{
                    Collection<R> coll = mapping.apply(e);
                    return coll != null ? coll.stream() : Stream.empty();
                })
                .filter(filter)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public static <T,R> List<R> flatMap(T[] array, Function<T, List<R>> mapping) {
        List<R> result = new ArrayList<>();
        for (T item : array) {
            result.addAll(mapping.apply(item));
        }
        return result;
    }

    public static String toString(Object object) {
        return object == null ? "" : object.toString();
    }

    public static <T> List<T> filterNot(Collection<T> list, Predicate<T> filter) {
        if(list == null) {
            return List.of();
        }
        return list.stream().filter(item -> !filter.test(item)).collect(Collectors.toList());

    }

    public static <T, R> R get(@Nullable T t, Function<T, R> mapping) {
        return t != null ? mapping.apply(t) : null;
    }

    public static <T> boolean listEquals(List<T> list1, List<T> list2) {
        if(list1.size() != list2.size()) {
            return false;
        }
        Iterator<T> it1 = list1.iterator(), it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if(!Objects.equals(it1.next(), it2.next())) {
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

    public static <T, M, R> R get(T t, Function<T, M> mapping1, Function<M, R> mapping2) {
        return get(get(t, mapping1), mapping2);
    }

    public static <T> void invokeIfNotNull(T t, Consumer<T> action) {
        if(t != null) {
            action.accept(t);
        }
    }

    public static <T, R> void getAndInvoke(T t, Function<T, R> mapper, Consumer<R> action) {
        invokeIfNotNull(get(t, mapper), action);
    }

    public static <T> T requireNonNull(T value) {
        return requireNonNull(value, "参数不能为空");
    }

    public static <T> T requireNull(T value) {
        return requireNull(value, "Value必须为空");
    }

    public static <T> T requireNull(@Nullable T value, String message) {
        return requireNull(value, () -> new InternalException(message));
    }

    public static <T> T requireNull(T value, Supplier<RuntimeException> exceptionSupplier) {
        if(value != null) {
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
        requireTrue(value >= min && value <= max, () -> new InternalException("参数必须在区间[" + min + "," + max+"]中"));
    }

    public static <T> T requireNonNull(T value, String message) {
        return requireNonNull(value, () -> new InternalException(message));
    }

    public static <T> T requireNonNull(T value, Supplier<RuntimeException> exceptionSupplier) {
        if(value == null) {
            throw exceptionSupplier.get();
        }
        return value;
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
        if(!value) {
            throw exceptionSupplier.get();
        }
    }

    public static void requireEquals(Object first, Object second, String message) {
        requireEquals(first, second, () -> new InternalException(message));
    }

    public static void requireEquals(Object first, Object second, Supplier<? extends RuntimeException> exceptionSupplier) {
        if(!Objects.equals(first, second)) {
            throw exceptionSupplier.get();
        }
    }

    @NotNull
    public static <T> List<T> requireNotEmpty(List<T> collection) {
        return requireNotEmpty(collection, "集合不能为空");
    }

    public static <T> List<T> requireNotEmpty(List<T> collection, String message) {
        if(isEmpty(collection)) {
            throw BusinessException.invalidParams(message);
        }
        return collection;
    }

    @SuppressWarnings("unused")
    public static <E> void addIfNotNull(List<E> list, E item) {
        if(item != null) {
            list.add(item);
        }
    }

    public static <T, R> List<R> splitAndMerge(Collection<T> source,
                                               Predicate<T> test,
                                               Function<List<T>, List<R>> loader1,
                                               Function<List<T>, List<R>> loader2)
    {
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
}
