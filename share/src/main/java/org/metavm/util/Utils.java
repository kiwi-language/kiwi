package org.metavm.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.invoke.SerializedLambda;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.*;

@Slf4j
public class Utils {

    public static final int BATCH_SIZE = 3000;

    public static final Pattern DIGITS_PTN = Pattern.compile("\\d+");

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH::mm:ss");

    public static void writeFile(String path, String content) {
        writeFile(new File(path), content);
    }

    public static void deleteFile(String path) {
        new File(path).delete();
    }

    @SneakyThrows
    public static void writeFile(File file, String content) {
        if (file.isDirectory())
            throw new IllegalArgumentException("Can not write to directory");
        if (!file.exists()) {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            file.createNewFile();
        }
        try (var fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        }
    }

    public static <T> boolean listEquals(List<T> list1, List<T> list2, BiPredicate<T, T> elementEquals) {
        if (list1.size() != list2.size())
            return false;
        var it1 = list1.iterator();
        var it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            var e1 = it1.next();
            var e2 = it2.next();
            if (!elementEquals.test(e1, e2))
                return false;
        }
        return true;
    }

    public static <T> boolean iterableEquals(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2) {
        var it1 = iterable1.iterator();
        var it2 = iterable2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!it1.next().equals(it2.next()))
                return false;
        }
        return !it1.hasNext() && !it2.hasNext();
    }
    @SneakyThrows
    public static void writeFile(Path path, byte[] bytes) {
        ensureDirectoryExists(path.getParent());
        try (var output = new FileOutputStream(path.toFile())) {
            output.write(bytes);
        }
    }

    public static String readLine(String path) {
        return readLine(new File(path));
    }

    @SneakyThrows
    public static String readLine(File file) {
        try (var scanner = new Scanner(new FileInputStream(file))) {
            return scanner.nextLine();
        }
    }

    public static void createDirectories(String path) {
        createDirectories(Path.of(path));
    }

    @SneakyThrows
    public static void createDirectories(Path path) {
        Files.createDirectories(path);
    }

    public static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    public static boolean bytesEquals(@Nullable byte[] bytes1, @Nullable byte[] bytes2) {
        if (bytes1 == bytes2)
            return true;
        if (bytes1 != null && bytes2 != null)
            return Arrays.equals(bytes1, bytes2);
        return false;
    }

    public static boolean isDigits(String str) {
        return DIGITS_PTN.matcher(str).matches();
    }

    public static void ensureDirectoryExists(String path) {
        ensureDirectoryExists(Path.of(path));
    }

    public static void ensureDirectoryExists(Path path) {
        var file = path.toFile();
        if (file.isFile())
            throw new IllegalArgumentException(String.format("Expecting '%s' to be a directory but it's a file", path));
        if (!file.exists())
            file.mkdirs();
    }

    public static <K, V> Map<K, V> zip(Iterable<? extends K> keys, Iterable<? extends V> values) {
        Map<K, V> map = new HashMap<>();
        biForEach(keys, values, map::put);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> List<T> merge(List<T> list1, List<T> list2, boolean desc) {
        Object[] array1 = list1.toArray(), array2 = list2.toArray();
        Arrays.sort(array1);
        Arrays.sort(array2);
        List<T> result = new ArrayList<>();
        T last = null;
        int i = 0, j = 0;
        while (i < array1.length && j < array2.length) {
            var t1 = (T) array1[i];
            var t2 = (T) array2[j];
            var r = t1.compareTo(t2);
            T x;
            if (r < 0) {
                x = t1;
                i++;
            } else if (r > 0) {
                x = t2;
                j++;
            } else {
                x = t1;
                i++;
                j++;
            }
            if (!x.equals(last))
                result.add(last = x);
        }
        for (; i < array1.length; i++) {
            var t1 = (T) array1[i];
            if (!t1.equals(last))
                result.add(last = t1);
        }
        for (; j < array2.length; j++) {
            var t2 = (T) array2[j];
            if (!t2.equals(last))
                result.add(last = t2);
        }
        if (desc)
            Collections.reverse(result);
        return result;
    }

    public static <T> List<T> concatList(List<? extends T> list, List<? extends T> values) {
        List<T> copy = new ArrayList<>(list);
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

    public static long readLong(String path) {
        return readLong(new File(path));
    }

    @SneakyThrows
    public static long readLong(File file) {
        try (var scanner = new Scanner(new FileReader(file))) {
            return scanner.nextLong();
        }
    }

    @SneakyThrows
    public static void writeLong(String path, long l) {
        try (var writer = new FileWriter(path)) {
            writer.write(Long.toString(l));
        }
    }

    public static void clearDirectory(String path) {
        clearDirectory(Path.of(path));
    }

    public static void clearDirectory(Path path) {
        if (Files.exists(path)) {
            try (var files = Files.walk(path)) {
                //noinspection ResultOfMethodCallIgnored
                files.sorted(Comparator.reverseOrder())
                        .filter(p -> !p.equals(path))
                        .forEach(f -> f.toFile().delete());
            } catch (IOException e) {
                System.err.println("Faileded to clear directory '" + path + "'");
                System.exit(1);
            }
        }
    }


    public static <T> T cast(Class<T> klass, Object object, String message) {
        if (klass.isInstance(object)) {
            return klass.cast(object);
        } else {
            throw new ClassCastException(message);
        }
    }

    public static <T> void forEach(Collection<T> collection, Consumer<T> action) {
        if (Utils.isEmpty(collection)) {
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

    public static <T> List<T> listOf(Iterable<T> iterable) {
        var list = new ArrayList<T>();
        for (T t : iterable) {
            list.add(t);
        }
        return list;
    }

    public static <T> void listAddAll(List<T> list, Iterable<? extends T> values) {
        values.forEach(list::add);
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

    public static String firstNonBlank(String s1, String s2, String s3) {
        if (!isBlank(s1))
            return s1;
        if (!isBlank(s2))
            return s2;
        if (!isBlank(s3))
            return s3;
        throw new NullPointerException("All strings are either null or empty");
    }

    public static String firstNonBlank(String s1, String s2, String s3, String s4) {
        if (!isBlank(s1))
            return s1;
        if (!isBlank(s2))
            return s2;
        if (!isBlank(s3))
            return s3;
        if (!isBlank(s4))
            return s4;
        throw new NullPointerException("All strings are either null or empty");
    }

    public static String firstNonBlank(String s1, String s2, String s3, String s4, String s5) {
        if (!isBlank(s1))
            return s1;
        if (!isBlank(s2))
            return s2;
        if (!isBlank(s3))
            return s3;
        if (!isBlank(s4))
            return s4;
        if (!isBlank(s5))
            return s5;
        throw new NullPointerException("All strings are either null or empty");
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

    public static boolean isNotBlank(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isBlank(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    public static boolean isEmptyValue(Object value) {
        if (value instanceof String str) {
            return isEmpty(str);
        }
        return value == null;
    }

    public static <T> long maxLong(List<T> list, Function<T, Long> mapping, long defaultMax) {
        return list.stream().map(mapping).mapToLong(Long::longValue)
                .max().orElse(defaultMax);
    }

    public static <T> int maxInt(List<T> list, ToIntFunction<T> mapper, int defaultMax) {
        return list.stream().mapToInt(mapper).max().orElse(defaultMax);
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
        return ThreadLocalRandom.current().nextLong();
    }

    public static long random(long bound) {
        return ThreadLocalRandom.current().nextLong(bound);
    }

    public static long randomNonNegative() {
        return random(9007199254740992L);
    }

    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    public static <T, R> @Nullable R first(List<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.getFirst()) : null;
    }

    public static <T, R> @Nullable R last(List<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.getLast()) : null;
    }

    public static <T, R> @Nullable R first(LinkedList<T> list, Function<T, R> mapping) {
        return isNotEmpty(list) ? mapping.apply(list.getFirst()) : null;
    }

    public static <T> @Nullable T first(List<T> list) {
        return first(list, Function.identity());
    }

    public static <T> @Nullable T last(List<T> list) {
        return last(list, Function.identity());
    }

    public static <T> @Nullable T first(LinkedList<T> list) {
        return first(list, Function.identity());
    }

    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> long count(Iterable<T> iterable, Predicate<T> filter) {
        return streamOf(iterable).filter(filter).count();
    }

    public static <T> int count(T[] array, Predicate<T> filter) {
        return (int) Arrays.stream(array).filter(filter).count();
    }

    public static final Pattern INT_PATTERN = Pattern.compile("^-?\\d+$");

    public static @Nullable Long tryParseLong(@Nullable String str) {
        if (str == null)
            return null;
        var matcher = INT_PATTERN.matcher(str);
        return matcher.matches() ? Long.parseLong(str) : null;
    }

    public static <T> List<T> filter(Iterable<T> source, Predicate<T> filter) {
        if (source == null) {
            return List.of();
        }
        return streamOf(source).filter(filter).collect(Collectors.toList());
    }

    public static <T> List<T> fillWith(T value, int times) {
        var list = new ArrayList<T>(times);
        for (int i = 0; i < times; i++) {
            list.add(value);
        }
        return list;
    }

    public static <T> Set<T> filterUnique(Iterable<T> source, Predicate<T> filter) {
        if (source == null) {
            return Set.of();
        }
        return streamOf(source).filter(filter).collect(Collectors.toSet());
    }


    public static <
            T> List<T> filterAndSort(Iterable<T> source, Predicate<T> filter, Comparator<T> comparator) {
        return filterAndSortAndLimit(source, filter, comparator, Long.MAX_VALUE);
    }

    public static <T> List<T> filterAndLimit(Iterable<T> source, Predicate<T> filter, long limit) {
        if (source == null) {
            source = List.of();
        }
        return streamOf(source).filter(filter).limit(limit).collect(Collectors.toList());
    }

    public static <
            T> List<T> filterAndSortAndLimit(Iterable<T> source, Predicate<T> filter, Comparator<T> comparator,
                                             long limit) {
        if (source == null) {
            source = List.of();
        }
        return streamOf(source).filter(filter).sorted(comparator).limit(limit).collect(Collectors.toList());
    }

    public static List<Long> range(long start, long end) {
        return LongStream.range(start, end).boxed().collect(Collectors.toList());
    }

    public static List<Integer> range(int start, int end) {
        return IntStream.range(start, end).boxed().collect(Collectors.toList());
    }

    public static <T> List<T> union(Collection<? extends T> coll1, Collection<? extends T> coll2) {
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

    public static <T> Set<T> mergeSets(List<Collection<? extends T>> sets) {
        var merged = new HashSet<T>();
        for (Collection<? extends T> set : sets) {
            if (set != null) {
                merged.addAll(set);
            }
        }
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

    public static <T> Set<T> mergeSets(Set<? extends T> coll1, Set<? extends T> coll2, Set<? extends
            T> coll3) {
        return mergeSets(mergeSets(coll1, coll2), coll3);
    }

    @SuppressWarnings("unused")
    public static <T> Set<T> mergeUnique(Collection<T> coll1, Collection<T> coll2) {
        Set<T> merged = new HashSet<>(coll1);
        merged.addAll(coll2);
        return merged;
    }

    public static <T> List<T> merge(Collection<? extends T> list1, Collection<? extends T> list2) {
        List<T> merged = new ArrayList<>(list1);
        merged.addAll(list2);
        return merged;
    }

    public static <T> List<T> merge(List<List<T>> collections) {
        List<T> merged = new ArrayList<>();
        for (List<T> coll : collections) {
            merged.addAll(coll);
        }
        return merged;
    }

    public static <T, M, R> List<R> mapAndFilterByType(T[] source, Function<T, M> mapper, Class<R> resultType) {
        var r = new ArrayList<R>();
        for (T t : source) {
            var m = mapper.apply(t);
            if (resultType.isInstance(m))
                r.add(resultType.cast(m));
        }
        return r;
    }

    public static <T, M, R> List<R> mapAndFilterByType(Iterable<T> source, Function<T, M> mapper, Class<R> resultType) {
        return Utils.filterByType(
                Utils.map(source, mapper),
                resultType
        );
    }

    public static <T> List<T> mergeSorted(List<? extends T> list1, List<? extends T> list2, Comparator<T> comparator) {
        if(list1.isEmpty())
            return new ArrayList<>(list2);
        if(list2.isEmpty())
            return new ArrayList<>(list1);
        var it1 = list1.iterator();
        var it2 = list2.iterator();
        var c1 = it1.next();
        var c2 = it2.next();
        var merged = new ArrayList<T>();
        for(;;) {
            if(comparator.compare(c1, c2) <= 0) {
                merged.add(c1);
                if (it1.hasNext())
                    c1 = it1.next();
                else {
                    merged.add(c2);
                    while (it2.hasNext())
                        merged.add(it2.next());
                    break;
                }
            }
            else {
                merged.add(c2);
                if (it2.hasNext())
                    c2 = it2.next();
                else {
                    merged.add(c1);
                    while (it1.hasNext())
                        merged.add(it1.next());
                    break;
                }
            }
        }
        return merged;
    }

    public static <T extends Comparable<T>> void forEachMerged(List<? extends T> list1, List<? extends T> list2,
                                                               Consumer<? super T> action) {
        forEachMerged(list1, list2, Comparable::compareTo, action);
    }

    public static <T> void forEachMerged(List<? extends T> list1, List<? extends T> list2,
                                         Comparator<T> comparator,
                                         Consumer<? super T> action) {
        if(list2.isEmpty())
            list1.forEach(action);
        else if(list1.isEmpty())
            list2.forEach(action);
        else {
            var it1 = list1.iterator();
            var it2 = list2.iterator();
            var e1 = it1.next();
            var e2 = it2.next();
            for(;;) {
                if(comparator.compare(e1, e2) <= 0) {
                    action.accept(e1);
                    if(it1.hasNext())
                        e1 = it1.next();
                    else {
                        action.accept(e2);
                        while (it2.hasNext())
                            action.accept(it2.next());
                        break;
                    }
                }
                else {
                    action.accept(e2);
                    if(it2.hasNext())
                        e2 = it2.next();
                    else {
                        action.accept(e1);
                        while (it1.hasNext())
                            action.accept(it1.next());
                        break;
                    }
                }
            }
        }
    }

    private static final Object endMark = new Object();

    public static <T extends Comparable<T>> Iterator<T> mergeIterator(Iterator<? extends T> it1, Iterator<? extends T> it2) {
        return mergeIterator(it1, it2, Comparable::compareTo);
    }

    public static <T> Iterator<T> mergeIterator(Iterator<? extends T> it1, Iterator<? extends T> it2, Comparator<T> comparator) {
        if(!it2.hasNext()) {
            //noinspection unchecked
            return (Iterator<T>) it1;
        }
        else if(!it1.hasNext()) {
            //noinspection unchecked
            return (Iterator<T>) it2;
        }
        else {
            return new Iterator<>() {
                private T e1 = it1.next();
                private T e2 = it2.next();

                @Override
                public boolean hasNext() {
                    return e1 != endMark || e2 != endMark;
                }

                @Override
                public T next() {
                    if(e1 != endMark && e2 != endMark) {
                        T r;
                        if(comparator.compare(e1, e2) <= 0) {
                            r = e1;
                            //noinspection unchecked
                            e1 = it1.hasNext() ? it1.next() : (T) endMark;
                        }
                        else {
                            r = e2;
                            //noinspection unchecked
                            e2 = it2.hasNext() ? it2.next() : (T) endMark;
                        }
                        return r;
                    }
                    else if(e1 != endMark) {
                        var r =  e1;
                        //noinspection unchecked
                        e1 = it1.hasNext() ? it1.next() : (T) endMark;
                        return r;
                    }
                    else if(e2 != endMark) {
                        var r =  e2;
                        //noinspection unchecked
                        e2 = it2.hasNext() ? it2.next() : (T) endMark;
                        return r;
                    }
                    else
                        throw new NoSuchElementException();
                }
            };
        }
    }

    public static <T, R> List<R> map(Iterable<T> source, Function<T, R> mapping) {
        if (source == null) {
            return List.of();
        }
        return streamOf(source).map(mapping).collect(Collectors.toList());
    }

    public static <T, R> R[] mapArray(T[] source, Function<T, R> mapper, IntFunction<R[]> generator) {
        return Arrays.stream(source).map(mapper).toArray(generator);
    }

    public static long decodeBase64(String str) {
        byte[] bytes = Base64.getDecoder().decode(str);
        long value = 0L;
        for (int i = 0; i < bytes.length; i++) {
            value |= (bytes[i] & 0xffL) << (i << 3);
        }
        return value;
    }

    public static String encodeBase64(long l) {
        return Base64.getEncoder().encodeToString(toBytes(l));
    }

    public static byte[] toBytes(long l) {
        int n = 8 - (Long.numberOfLeadingZeros(l) >> 3);
        if (n == 0)
            n = 1;
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) (l >> (i << 3));
        }
        return bytes;
    }

    public static <T, R> Map<R, Integer> mapAndCount(Iterable<T> list, Function<T, R> keyMapper) {
        Map<R, Integer> countMap = new HashMap<>();
        for (T item : list) {
            R key = keyMapper.apply(item);
            countMap.compute(key, (k, old) -> old != null ? old + 1 : 1);
        }
        return countMap;
    }

    public static <
            T, M, R> List<R> map(Collection<T> source, Function<T, M> mapping1, Function<M, R> mapping2) {
        return map(map(source, mapping1), mapping2);
    }

    @SuppressWarnings("unused")
    public static <
            T, K, V> Map<K, List<V>> groupBy(List<T> list, Function<T, K> keyMapping, Function<T, V> valueMapping) {
        Map<K, List<V>> result = new HashMap<>();
        for (T t : list) {
            K k = keyMapping.apply(t);
            result.computeIfAbsent(k, k1 -> new ArrayList<>()).add(valueMapping.apply(t));
        }
        return result;
    }

    public static <T, R> Set<R> mapToSet(Iterable<T> source, Function<T, R> mapping) {
        if (source == null) {
            return Set.of();
        }
        return streamOf(source).map(mapping).collect(Collectors.toSet());
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

    public static <T> List<T> sort(Iterable<T> iterable, Comparator<T> comparator) {
        if (iterable == null)
            return List.of();
        return streamOf(iterable).sorted(comparator).collect(Collectors.toList());
    }

    public static <T> List<T> sortByInt(Iterable<T> list, ToIntFunction<T> intMapper) {
        return sortAndMap(list, Comparator.comparingInt(intMapper), Function.identity());
    }

    public static <
            T, R> List<R> sortByIntAndMap(Iterable<T> list, ToIntFunction<T> intFunc, Function<T, R> mapper) {
        return sortAndMap(list, Comparator.comparingInt(intFunc), mapper);
    }

    public static <
            T, R> List<R> sortAndMap(Iterable<T> iterable, Comparator<T> comparator, Function<T, R> mapper) {
        if (iterable == null) {
            return List.of();
        }
        return streamOf(iterable)
                .sorted(comparator)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <
            T, R> List<R> mapAndSort(Iterable<T> iterable, Function<T, R> mapper, Comparator<R> comparator) {
        if (iterable == null) {
            return List.of();
        }
        return streamOf(iterable)
                .map(mapper)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Foreach that allows the action to modify the iterable without raising a ConcurrentModificationException
     */
    public static <T> void enhancedForEach(Iterable<T> iterable, Consumer<T> action) {
        if (iterable != null) {
            var list = new ArrayList<T>();
            iterable.forEach(list::add);
            list.forEach(action);
        }
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
        return isNotEmpty(list) ? mapping.apply(list.getFirst()) : null;
    }

    public static <T, R> void biForEach(T[] array1, R[] array2, BiConsumer<T, R> action) {
        int len = array1.length;
        if (len != array2.length)
            throw new IllegalArgumentException("Both arrays must have the same length");
        for (int i = 0; i < len; i++)
            action.accept(array1[i], array2[i]);
    }

    public static <T, R> void biForEach(Iterable<T> list1, Iterable<R> list2, BiConsumer<T, R> action) {
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext())
            action.accept(it1.next(), it2.next());
        if (it1.hasNext() || it2.hasNext())
            throw new RuntimeException("Both lists must have the same size");
    }

    public static <T, K> void forEachPair(Collection<T> list1, Collection<T> list2,
                                          Function<T, K> keyMapper, BiConsumer<T, T> action) {
        var map = new HashMap<K, T>();
        list1.forEach(t1 -> map.put(keyMapper.apply(t1), t1));
        for (T t2 : list2)
            action.accept(map.remove(keyMapper.apply(t2)), t2);
        map.values().forEach(t1 -> action.accept(t1, null));
    }

    @SuppressWarnings({"ReassignedVariable", "unchecked"})
    public static <T extends Comparable<T>> void forEachPair
            (Collection<T> coll1, Collection<T> coll2, BiConsumer<T, T> action) {
        var array1 = coll1.toArray();
        var array2 = coll2.toArray();
        Arrays.sort(array1);
        Arrays.sort(array2);
        int i = 0, j = 0;
        while (i < array1.length && j < array2.length) {
            var t1 = (T) array1[i];
            var t2 = (T) array2[j];
            var r = t1.compareTo(t2);
            if (r < 0) {
                action.accept(t1, null);
                i++;
            } else if (r > 0) {
                action.accept(null, t2);
                j++;
            } else {
                action.accept(t1, t2);
                i++;
                j++;
            }
        }
        for (; i < array1.length; i++) {
            action.accept((T) array1[i], null);
        }
        for (; j < array2.length; j++) {
            action.accept(null, (T) array2[j]);
        }
    }

    public static <T> List<T> multipleOf(T value, int size) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(value);
        }
        return list;
    }

    public static <
            T1, T2, R> List<R> biMap(List<T1> list1, List<T2> list2, BiFunction<T1, T2, R> mapper) {
        if (list1 == null) {
            list1 = List.of();
        }
        if (list2 == null) {
            list2 = List.of();
        }
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

    public static <
            T, R> List<R> filterAndMap(Iterable<T> source, Predicate<T> filter, Function<T, R> mapping) {
        if (source == null) {
            return List.of();
        }
        return streamOf(source)
                .filter(filter)
                .map(mapping)
                .collect(Collectors.toList());
    }

    public static <T> void filterAndForeach(Iterable<T> source, Predicate<T> filter, Consumer<T> action) {
        if (source == null) {
            return;
        }
        streamOf(source)
                .filter(filter)
                .forEach(action);
    }


    public static <T, R> List<R> filterByType(Iterable<T> source, Class<R> type) {
        return filterAndMap(source, type::isInstance, type::cast);
    }

    public static <S, T, R> List<R> filterByTypeAndMap(Iterable<S> source, Class<T> type, Function<T, R> mapper) {
        return filterAndMap(source, type::isInstance, v -> mapper.apply(type.cast(v)));
    }

    @SuppressWarnings("unused")
    public static <
            T, R> Set<R> filterAndMapUnique(Iterable<T> source, Predicate<T> filter, Function<T, R> mapping) {
        if (source == null) {
            return Set.of();
        }
        return streamOf(source)
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

    public static <T> @Nullable T find(Iterable<T> iterable, Predicate<? super T> filter) {
        if (iterable == null) {
            return null;
        }
        return streamOf(iterable).filter(filter).findAny().orElse(null);
    }

    public static <T, R> @Nullable R findAndMap(Iterable<T> iterable, Predicate<? super T> filter,
                                                Function<? super T, ? extends R> mapper) {
        return streamOf(iterable).filter(filter).findFirst().map(mapper).orElse(null);
    }

    public static <T> boolean exists(Iterable<T> iterable, Predicate<T> filter) {
        return find(iterable, filter) != null;
    }

    public static <T> @Nullable T find(T[] array, Predicate<T> filter) {
        return Arrays.stream(array).filter(filter).findAny().orElse(null);
    }

    public static <T> @Nullable T findByType(Object[] array, Class<T> type) {
        return type.cast(find(array, type::isInstance));
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

    public static <T> void splitAndForEach
            (Collection<T> collection, Predicate<T> test, Consumer<List<T>> action1, Consumer<List<T>> action2) {
        List<T> list1 = filter(collection, test);
        List<T> list2 = exclude(collection, test);
        if (isNotEmpty(list1)) {
            action1.accept(list1);
        }
        if (isNotEmpty(list2)) {
            action2.accept(list2);
        }
    }

    public static <T> T findRequired(T[] array, Predicate<T> filter) {
        return Arrays.stream(array).filter(filter).findAny()
                .orElseThrow(NoSuchElementException::new);
    }

    public static <T> T findRequired(T[] array, Predicate<T> filter, Supplier<String> messageSupplier) {
        return Arrays.stream(array).filter(filter).findAny()
                .orElseThrow(() -> new NoSuchElementException(messageSupplier.get()));
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

    public static <T> int indexOf(T[] array, T o) {
        for (int i = 0; i < array.length; i++) {
            if(array[i].equals(o))
                return i;
        }
        return -1;
    }

    public static <T> T findRequired(Iterable<T> iterable, Predicate<T> filter) {
        return findRequired(iterable, filter, "value not found");
    }

    public static <T> T findRequired(Iterable<T> iterable, Predicate<T> filter, String message) {
        return findRequired(iterable, filter, () -> message);
    }

    public static <T> T findRequired(Iterable<T> iterable, Predicate<T> filter,
                                     Supplier<String> messageSupplier) {
        if (iterable == null) {
            throw new NullPointerException(messageSupplier.get());
        }
        return streamOf(iterable).filter(filter).findAny()
                .orElseThrow(() -> new NullPointerException(messageSupplier.get()));
    }

    private static <T> Stream<T> streamOf(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @SuppressWarnings("unused")
    public static <T> void invokeIfNotEmpty(Collection<T> collection, Consumer<Collection<T>> action) {
        if (isNotEmpty(collection)) {
            action.accept(collection);
        }
    }

    public static <T> T filterOneRequired(Iterable<T> iterable, Predicate<T> filter, String errorMessage) {
        return streamOf(iterable).filter(filter).findAny().orElseThrow(() -> new RuntimeException(errorMessage));
    }

    @Nullable
    public static <T, R> R
    filterOneAndMap(Iterable<T> iterable, Predicate<T> filter, Function<T, R> mapping) {
        return streamOf(iterable).filter(filter).map(mapping).findAny().orElse(null);
    }

    @SuppressWarnings("unused")
    public static <T> String
    filterAndJoin(Iterable<T> iterable, Predicate<T> filter, Function<T, String> mapping) {
        return join(iterable, filter, mapping, ",");
    }

    public static <T> String join(Iterable<T> list, Function<T, String> mapping) {
        return join(list, mapping, ",");
    }


    public static <T> String join(T[] array, Function<T, String> mapping) {
        return join(Arrays.asList(array), mapping);
    }

    public static <T> String join(Iterable<T> iterable, Function<T, String> mapping, String delimiter) {
        return join(iterable, t -> true, mapping, delimiter);
    }

    public static <T> String
    join(Iterable<T> iterable, Predicate<T> filter, Function<T, String> mapping, String delimiter) {
        return streamOf(iterable).filter(filter).map(mapping).collect(Collectors.joining(delimiter));
    }

    public static String join(Iterable<String> strList) {
        return join(strList, ",");
    }

    public static String join(Iterable<String> strList, String delimiter) {
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

    public static <K, T, R> Map<K, R> toMap(Iterable<T> iterable, Function<T, ? extends
            K> keyMapper, Function<T, ? extends R> valueMapper) {
        if (iterable == null) {
            return new HashMap<>();
        }
        Map<K, R> map = new HashMap<>();
        for (T item : iterable) {
            map.put(keyMapper.apply(item), valueMapper.apply(item));
        }
        return map;
    }

    public static <
            K, T> IdentityHashMap<K, T> toIdentityMap(Collection<T> list, Function<T, K> keyMapping) {
        if (list == null) {
            return new IdentityHashMap<>();
        }
        IdentityHashMap<K, T> map = new IdentityHashMap<>();
        for (T item : list) {
            map.put(keyMapping.apply(item), item);
        }
        return map;
    }

    public static <K, T> Map<K, List<T>> toMultiMap(Iterable<T> list, Function<T, K> keyMapping) {
        return toMultiMap(list, keyMapping, Function.identity());
    }

    public static <
            K, T, R> Map<K, List<R>> toMultiMap(Iterable<T> list, Function<T, K> keyMapper, Function<T, R> valueMapper) {
        if (list == null) {
            return new HashMap<>();
        }
        Map<K, List<R>> map = new HashMap<>();
        for (T item : list) {
            map.computeIfAbsent(keyMapper.apply(item), k -> new ArrayList<>()).add(valueMapper.apply(item));
        }
        return map;
    }

    public static <
            T, R> List<R> mapAndFilter(Iterable<T> source, Function<T, R> mapping, Predicate<R> filter) {
        if (source == null) {
            return List.of();
        }
        return streamOf(source)
                .map(mapping)
                .filter(filter)
                .collect(Collectors.toList());
    }

    public static <
            T, R> Set<R> mapAndFilterUnique(Iterable<T> source, Function<T, R> mapping, Predicate<R> filter) {
        if (source == null) {
            return Set.of();
        }
        return streamOf(source)
                .map(mapping)
                .filter(filter)
                .collect(Collectors.toSet());
    }

    public static <T, R> Map<T, R> buildMap(List<T> list1, Collection<R> list2) {
        require(list1.size() == list2.size(), "both list must have same size");
        Map<T, R> map = new LinkedHashMap<>();
        Iterator<T> it1 = list1.iterator();
        Iterator<R> it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            map.put(it1.next(), it2.next());
        }
        return map;
    }

    @SuppressWarnings("unused")
    public static <T> List<T> deduplicate(Collection<T> list) {
        return new ArrayList<>(new HashSet<>(list));
    }

    public static <T, K> List<T> deduplicate(List<T> list, Function<T, K> keyMapper) {
        Set<K> set = new HashSet<>();
        List<T> result = new ArrayList<>();
        for (T t : list) {
            var key = keyMapper.apply(t);
            if (set.add(key))
                result.add(t);
        }
        return result;
    }


    public static <T, R> List<R> flatMap(Iterable<T> list, Function<T, Collection<R>> mapping) {
        return flatMapAndFilter(list, mapping, e -> true);
    }

    public static <
            T, R> List<R> flatMapAndFilter(Iterable<T> iterable, Function<T, Collection<R>> mapping, Predicate<R> filter) {
        if (iterable == null) {
            return List.of();
        }
        return streamOf(iterable)
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

    public static <T> List<T> exclude(Iterable<? extends T> iterable, Predicate<T> filter) {
        if (iterable == null) {
            return List.of();
        }
        return streamOf(iterable).filter(item -> !filter.test(item)).collect(Collectors.toList());

    }

    private final static char[] passwordChars;

    static {
        passwordChars = new char[10 + 26 + 26];
        int i = 0;
        for (int j = 0; j < 10; j++) {
            passwordChars[i++] = (char) ('0' + j);
        }
        for (int j = 0; j < 26; j++) {
            passwordChars[i++] = (char) ('A' + j);
        }
        for (int j = 0; j < 26; j++) {
            passwordChars[i++] = (char) ('a' + j);
        }
    }

    public static String randomPassword() {
        var random = ThreadLocalRandom.current();
        int len = random.nextInt(8, 12);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < len; i++) {
            buf.append(passwordChars[random.nextInt(passwordChars.length)]);
        }
        return buf.toString();
    }

    @Nullable
    public static <T, R> R safeCall(@Nullable T t, Function<T, R> mapping) {
        return t != null ? mapping.apply(t) : null;
    }

    @Nullable
    public static <T, R> R safeCall(@Nullable T t, Function<T, R> mapping, Supplier<R> defaultSupplier) {
        return t != null ? mapping.apply(t) : defaultSupplier.get();
    }

    public static <T, R> R safeCall(@Nullable T t, Function<T, R> mapping, R defaultValue) {
        return t != null ? mapping.apply(t) : defaultValue;
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

    public static <T> T orElse(@Nullable T t, T elseValue) {
        return mapOrElse(t, Function.identity(), () -> elseValue);
    }

    public static <T> T orElse(@Nullable T t, Supplier<T> elseSupplier) {
        return mapOrElse(t, Function.identity(), elseSupplier);
    }

    public static <T, R> R mapOrElse(T t, Function<T, R> mapping, Supplier<R> elseSupplier) {
        return t != null ? mapping.apply(t) : elseSupplier.get();
    }

    @Nullable
    public static <T, M, R> R safeCall(@Nullable T t, Function<T, M> mapping1, Function<M, R> mapping2) {
        return safeCall(safeCall(t, mapping1), mapping2);
    }

    public static <T> void ifNotNull(@Nullable T t, Consumer<? super T> action) {
        if (t != null)
            action.accept(t);
    }

    public static <T> void ifTrue(T t, Predicate<? super T> test, Consumer<? super T> action) {
        if (test.test(t))
            action.accept(t);
    }

    public static <T> void ifFalse(T t, Predicate<? super T> test, Consumer<? super T> action) {
        if (!test.test(t))
            action.accept(t);
    }

    public static <T, R> void getAndInvoke(T t, Function<T, R> mapper, Consumer<R> action) {
        ifNotNull(safeCall(t, mapper), action);
    }

    public static int compareId(@Nullable Long l1, @Nullable Long l2) {
        if (l1 == null)
            l1 = Long.MAX_VALUE;
        if (l2 == null)
            l2 = Long.MAX_VALUE;
        return l1.compareTo(l2);
    }

    public static <T> boolean equalsIgnoreOrder(Collection<? extends T> coll1, Collection<? extends T> coll2) {
        if (coll1.size() != coll2.size())
            return false;
        return new HashSet<>(coll1).equals(new HashSet<>(coll2));
    }

    public static void require(boolean value) {
        if (!value)
            throw new IllegalStateException();
    }

    public static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }

    public static void require(boolean value, Supplier<String> messageSupplier) {
        if (!value)
            throw new IllegalStateException(messageSupplier.get());
    }

    @NotNull
    public static <T> Collection<T> requireNotEmpty(Collection<T> collection) {
        if (collection == null || collection.isEmpty())
            throw new IllegalStateException("Collection must not be empty");
        else
            return collection;
    }

    public static <T> Collection<T> requireNotEmpty(Collection<T> collection, String message) {
        if (collection == null || isEmpty(collection))
            throw new IllegalStateException(message);
        return collection;
    }

    public static String escape(String str) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            buf.append(escape(str.charAt(i)));
        }
        return buf.toString();
    }

    public static String escape(char c) {
        if (c == '\\')
            return "\\\\";
        else if (c == '\"')
            return "\"";
        else
            return Character.toString(c);
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
        List<T> list2 = exclude(source, test);
        List<R> result1 = isNotEmpty(list1) ? loader1.apply(list1) : List.of();
        List<R> result2 = isNotEmpty(list2) ? loader2.apply(list2) : List.of();
        return union(result1, result2);
    }

    public static <T> boolean anyMatch(Iterable<T> iterable, Predicate<T> test) {
        return streamOf(iterable).anyMatch(test);
    }

    public static <T> boolean anyMatch(T[] array, Predicate<T> test) {
        for (T t : array) {
            if (test.test(t)) return true;
        }
        return false;
    }

    public static <T1, T2> boolean biAllMatch(Iterable<T1> iterable1,
                                              Iterable<T2> iterable2,
                                              BiPredicate<T1, T2> predicate) {
        var it1 = iterable1.iterator();
        var it2 = iterable2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!predicate.test(it1.next(), it2.next())) {
                return false;
            }
        }
        require(!it1.hasNext() && !it2.hasNext());
        return true;
    }

    public static <T> boolean allMatch(Iterable<T> iterable, Predicate<T> predicate) {
        if (iterable == null) {
            return true;
        }
        return streamOf(iterable).allMatch(predicate);
    }

    public static <T> boolean equalsIgnoreOrder(Collection<? extends T> coll1, Collection<? extends T> coll2, BiPredicate<T, T> equals) {
        if (coll1.size() != coll2.size())
            return false;
        var list = new LinkedList<>(coll2);
        out:
        for (T t : coll1) {
            var it = list.iterator();
            while (it.hasNext()) {
                if (equals.test(t, it.next())) {
                    it.remove();
                    continue out;
                }
            }
            return false;
        }
        return true;
    }

    public static <T> boolean noneMatch(Iterable<T> iterable, Predicate<T> predicate) {
        for (T t : iterable) {
            if (predicate.test(t))
                return false;
        }
        return true;
    }

    public static <K, V> Map<K, V> cloneMap(Map<K, V> map, Function<V, V> valueClone) {
        var clone = new HashMap<K, V>();
        map.forEach((k, v) -> clone.put(k, valueClone.apply(v)));
        return clone;
    }

    public static boolean allTrue(List<Boolean> booleans) {
        return Utils.allMatch(booleans, b -> b);
    }

    public static <T, R> Set<R> flatMapUnique(Iterable<T> iterable, Function<T, ? extends Collection<R>>
            mapper) {
        if (iterable == null) {
            return Set.of();
        }
        return streamOf(iterable)
                .flatMap(e -> {
                    Collection<R> coll = mapper.apply(e);
                    return coll != null ? coll.stream() : Stream.empty();
                })
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    public static SerializedLambda getSerializedLambda(Serializable lambda) {
        try (var output = new MyObjectOutput(new ByteArrayOutputStream())) {
            output.writeObject(lambda);
            return output.getSerializedLambda();
        }
    }

    public static <T> void doInBatch(List<T> records, Consumer<List<T>> action) {
        for (int start = 0; start < records.size(); start += BATCH_SIZE) {
            action.accept(records.subList(start, Math.min(start + BATCH_SIZE, records.size())));
        }
    }

    public static boolean fileExists(String path) {
        return Files.exists(Path.of(path));
    }

    public static CommandResult executeCommand(Path directory, String... command) {
        log.info("Executing command {} in working dir {}",
                String.join(" ", command), directory.toString());
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        // Set the working directory for the command
        processBuilder.directory(directory.toFile());

        // Merge the standard output and standard error streams.
        // This makes it easier to capture all output.
        processBuilder.redirectErrorStream(true);

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Use a StringBuilder to capture the command's output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Wait for the process to complete (with a timeout)
        boolean finished;
        try {
            finished = process.waitFor(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out.");
        }

        return new CommandResult(process.exitValue(), output.toString());
    }

    public static int compareBytes(byte[] bs1, byte[] bs2) {
        var len = Math.min(bs1.length, bs2.length);
        for (int i = 0; i < len; i++) {
            var b1 = (int) bs1[i] & 0xff;
            var b2 = (int) bs2[i] & 0xff;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        if (bs1.length > len)
            return 1;
        if (bs2.length > len)
            return -1;
        return 0;
    }

    private static class MyObjectOutput extends ObjectOutputStream {

        private SerializedLambda serializedLambda;

        public MyObjectOutput(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
            if (obj instanceof SerializedLambda ser) {
                this.serializedLambda = ser;
            }
            return obj;
        }

        public SerializedLambda getSerializedLambda() {
            return serializedLambda;
        }
    }

    @SneakyThrows
    public static String getSchemaAndHost(String url) {
        var u = new URI(url);
        return u.getScheme() + "://" + u.getHost();
    }

    public static Throwable getRootCause(Throwable t) {
        var e = t;
        while (e.getCause() != null)
            e = e.getCause();
        return e;
    }

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static <T> int findIndex(List<T> list, Predicate<T> predicate) {
        int i = 0;
        for (T t : list) {
            if (predicate.test(t))
                return i;
            i++;
        }
        return -1;
    }

    public static <T> void replaceOrAppend(List<T> list, T element, BiPredicate<T,T> equals) {
        if(!replace(list, element,equals))
            list.add(element);
    }

    public static <T> boolean replace(List<T> list, T element, BiPredicate<T, T> equals) {
        var it = list.listIterator();
        while (it.hasNext()) {
            var e = it.next();
            if(equals.test(e, element)) {
                it.remove();
                it.add(element);
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public static List<Path> listFilePathsRecursively(Path start, String suffix) {
        if (!Files.exists(start) || !Files.isDirectory(start)) {
            throw new IllegalArgumentException("Provided path is not an existing directory: " + start);
        }

        try (Stream<Path> stream = Files.walk(start)) {
            return stream
                    .filter(f -> Files.isRegularFile(f) && f.getFileName().toString().endsWith(suffix))
                    .map(Path::toAbsolutePath) // Convert Path to absolute Path
                    .collect(Collectors.toList());
        }
    }

    public static <T, V> Iterator<V> mapIterator(Iterator<T> source, Function<T, V> mapper) {
        return new Iterator<V>() {
            @Override
            public boolean hasNext() {
                return source.hasNext();
            }

            @Override
            public V next() {
                return mapper.apply(source.next());
            }

            @Override
            public void remove() {
                source.remove();
            }
        };
    }

    public static String repeatWithDelimiter(String s, int count, String delimiter) {
        if (count == 0) return "";
        else if (count == 1) return s;
        return s + (delimiter + s).repeat(count - 1);
    }

    public record CommandResult(int exitCode, String output) {}
}
