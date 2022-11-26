package tech.metavm.util;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Table<T> extends LinkedList<T> {

    public static final int DEFAULT_INDEX_BUILD_THRESHOLD = 3;

    private Long id;
    private long version;
    private long syncVersion;
    private boolean loaded;
    private final Supplier<Collection<T>> loader;
    private final int buildIndexThreshold;
    private final Map<IndexDesc<T>, Integer> counterMap = new HashMap<>();
    private final Map<IndexDesc<T>, Map<Object, LinkedList<Node<T>>>> indexes = new HashMap<>();

    public Table(Collection<T> data) {
        this(data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public Table(int buildIndexThreshold) {
        this(List.of(), buildIndexThreshold);
    }

    public Table() {
        this(List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public Table(Collection<T> data, int buildIndexThreshold) {
        this.buildIndexThreshold = buildIndexThreshold;
        addAll(data);
        loader = null;
    }

    public Table(Supplier<Collection<T>> loader) {
        this(loader, DEFAULT_INDEX_BUILD_THRESHOLD);
    }
    public Table(Supplier<Collection<T>> loader, int buildIndexThreshold) {
        this.buildIndexThreshold = buildIndexThreshold;
        this.loader = loader;
    }

    public <K> T get(IndexMapper<T, K> keyMapper, K key) {
        beforeAccess();
        return NncUtils.get(getNode(keyMapper, key), Node::getValue);
    }

    public <K> T remove(IndexMapper<T, K> keyMapper, K key) {
        Node<T> node = getNode(keyMapper, key);
        if(node != null) {
            removeNode(node);
            return node.getValue();
        }
        return null;
    }

    protected  <K> Node<T> getNode(IndexMapper<T, K> keyMapper, K key) {
        Map<?, LinkedList<Node<T>>> index = tryGetIndex(keyMapper);
        if(index != null) {
            return NncUtils.getFirst(index.get(key));
        }
        else {
            return findNode(v -> Objects.equals(keyMapper.apply(v), key));
        }
    }

    public <K> List<T> filter(IndexMapper<T, K> keyMapper, K key) {
        beforeAccess();
        Map<?, LinkedList<Node<T>>> index = tryGetIndex(keyMapper);
        if(index != null) {
            return NncUtils.map(index.get(key), Node::getValue);
        }
        else {
            return filter(value -> Objects.equals(keyMapper.apply(value), key));
        }
    }

    public Long getId() {
        return id;
    }

    private <K> Map<?, LinkedList<Node<T>>> tryGetIndex(IndexMapper<T, K> keyMapper) {
        IndexDesc<T> indexDesc = IndexDesc.create(keyMapper);
        Map<?, LinkedList<Node<T>>> index = indexes.get(indexDesc);
        if(index != null) {
            return index;
        }
        int count = counterMap.compute(indexDesc, (k, o) -> o == null ? 1 : o + 1);
        if(count >= buildIndexThreshold) {
            return buildIndex0(indexDesc);
        }
        return null;
    }

    public <K> void buildIndex(IndexMapper<T, K> keyMapper) {
        beforeAccess();
        IndexDesc<T> indexDesc = IndexDesc.create(keyMapper);
        buildIndex(indexDesc);
    }

    public <K> void buildIndex(IndexDesc<T> indexDesc) {
        if(!indexes.containsKey(indexDesc)) {
            buildIndex0(indexDesc);
        }
    }

    @Override
    public boolean remove(Object value) {
        beforeAccess();
        Node<T> node = getEquals(value);
        if(node != null) {
            removeNode(node);
            return true;
        }
        else {
            return false;
        }
    }

    private <K> Map<Object, LinkedList<Node<T>>>  buildIndex0(IndexDesc<T> indesDesc) {
        Map<Object, LinkedList<Node<T>>> index = new HashMap<>();
        forEachNode(
                node -> index.computeIfAbsent(
                        indesDesc.map(node.getValue()),
                        k->new LinkedList<>()
                ).add(node)
        );
        indexes.put(indesDesc, index);
        return index;
    }

    protected void onAdd(Node<T> node) {
        indexes.forEach((indexDesc, index) ->
            index.computeIfAbsent(indexDesc.map(node.getValue()), k->new LinkedList<>()).add(node)
        );
    }

    protected void onRemove(Node<T> node) {
        indexes.forEach((keyMapper, index) -> {
            LinkedList<Node<T>> list = index.get(keyMapper.map(node.getValue()));
            ListIterator<Node<T>> it = list.listIterator();
            while(it.hasNext()) {
                if(it.next() == node) {
                    it.remove();
                    break;
                }
            }
        });
    }

    @Override
    public boolean contains(Object o) {
        beforeAccess();
        return getEquals(o) != null;
    }

    private LinkedList.Node<T> getEquals(Object o) {
        @SuppressWarnings ("unchecked") T t =  (T) o;
        if(!indexes.isEmpty()) {
            Map.Entry<IndexDesc<T>, Map<Object, LinkedList<Node<T>>>> entry
                    = indexes.entrySet().iterator().next();
            return NncUtils.getFirst(entry.getValue().get(entry.getKey().map(t)));
        }
        return findNode(v -> Objects.equals(v, o));
    }

    public Table<T> merge(Table<T> that) {
        Table<T> merged = new Table<>(buildIndexThreshold);
        indexes.keySet().forEach(merged::buildIndex);
        merged.addAll(this);
        merged.addAll(that);
        return merged;
    }

    @Override
    protected void beforeAccess() {
        if(loader != null && !loaded) {
            Collection<T> data = loader.get();
            for (T datum : data) {
                addLast0(datum);
            }
            loaded = true;
        }
    }

    public <R> Table<R> map(Function<T, R> mapper) {
        return mapAndFilter(mapper, t -> true);
    }

    public <R> Table<R> mapAndFilter(Function<T, R> mapper, Predicate<R> filter) {
        beforeAccess();
        Table<R> that = new Table<>();
        for (T t : this) {
            R r = mapper.apply(t);
            if(filter.test(r)) {
                that.add(r);
            }
        }
        return that;
    }

    public long getVersion() {
        return version;
    }

    public long getSyncVersion() {
        return syncVersion;
    }

    private Table<T> createSame() {
        Table<T> that = new Table<>(buildIndexThreshold);
        for (IndexDesc<T> indexDesc : indexes.keySet()) {
            that.buildIndex(indexDesc);
        }
        return that;
    }

    private static final class IndexDesc<T> {
        private final String implClass;
        private final String implMethodName;
        private final List<Object> capturedArgs;
        private final IndexMapper<T, ?> indexMapper;

        private IndexDesc(
                String implClass,
                String implMethodName,
                List<Object> capturedArgs,
                IndexMapper<T, ?> indexMapper) {
            this.implClass = implClass;
            this.implMethodName = implMethodName;
            this.capturedArgs = capturedArgs;
            this.indexMapper = indexMapper;
        }

        public static <T> IndexDesc<T> create(IndexMapper<T, ?> mapper) {
                Method writeReplaceMethod = ReflectUtils.getMethodByName(mapper.getClass(), "writeReplace");
                SerializedLambda serializedLambda = (SerializedLambda) ReflectUtils.invoke(mapper, writeReplaceMethod);
                List<Object> capturedArgs = new ArrayList<>();
                for (int i = 0; i < serializedLambda.getCapturedArgCount(); i++) {
                    capturedArgs.add(serializedLambda.getCapturedArg(i));
                }
                return new IndexDesc<>(
                        serializedLambda.getImplClass(),
                        serializedLambda.getImplMethodName(),
                        capturedArgs,
                        mapper
                );
            }

        public Object map(T t) {
            return indexMapper.apply(t);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (IndexDesc<?>) obj;
            return Objects.equals(this.implClass, that.implClass) &&
                    Objects.equals(this.implMethodName, that.implMethodName) &&
                    Objects.equals(this.capturedArgs, that.capturedArgs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(implClass, implMethodName, capturedArgs);
        }

    }

    public interface IndexMapper<T, K> extends Function<T, K>, Serializable {

    }

}
