package tech.metavm.util;

import tech.metavm.entity.ModelIdentity;
import tech.metavm.entity.IdInitializing;
import tech.metavm.entity.NoProxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Table<T> extends LinkedList<T> implements IdInitializing, RuntimeGeneric {

    public static final int DEFAULT_INDEX_BUILD_THRESHOLD = 3;

    public static  <T> Table<T> createProxy(Class<? extends Table<T>> proxyClass, Type elementType) {
        return ReflectUtils.invokeConstructor(
                ReflectUtils.getConstructor(proxyClass, Type.class),
                elementType
        );
    }

    private Long id;
    private final Type genericType;
    private final Type elementType;
    private final int buildIndexThreshold;
    private final Map<IndexDesc<T>, Integer> counterMap = new HashMap<>();
    private final Map<IndexDesc<T>, Map<Object, LinkedList<Node<T>>>> indexes = new HashMap<>();
    private boolean elementAsChild;
    private ModelIdentity identifier;

    public Table(Class<T> klass, Collection<T> data) {
        this(TypeReference.of(klass), data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public Table(TypeReference<T> typeRef, Collection<T> data) {
        this(typeRef, data, DEFAULT_INDEX_BUILD_THRESHOLD);
    }


    public Table(Class<T> klass, int buildIndexThreshold) {
        this(TypeReference.of(klass), List.of(), buildIndexThreshold);
    }

    public Table(TypeReference<T> typeRef, int buildIndexThreshold) {
        this(typeRef, List.of(), buildIndexThreshold);
    }

    public Table(Class<T> klass) {
        this(TypeReference.of(klass), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public Table(Class<T> klass, boolean elementAsChild) {
        this(TypeReference.of(klass).getType(), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD, elementAsChild);
    }

    public Table(TypeReference<T> typeRef) {
        this(typeRef, List.of(), DEFAULT_INDEX_BUILD_THRESHOLD);
    }

    public Table(TypeReference<T> typeRef, boolean elementAsChild) {
        this(typeRef.getType(), List.of(), DEFAULT_INDEX_BUILD_THRESHOLD, elementAsChild);
    }

    public Table(TypeReference<T> typeRef, Collection<T> data, int buildIndexThreshold) {
        this(typeRef.getGenericType(), data, buildIndexThreshold, false);
    }

    public Table(Type elementType) {
        this(elementType, List.of(), DEFAULT_INDEX_BUILD_THRESHOLD, false);
    }

    private Table(Type elementType, Collection<T> data, int buildIndexThreshold, boolean elementAsChild) {
        this.elementType = elementType;
        this.genericType = new ParameterizedTypeImpl(
                null,
                Table.class,
                new Type[]{ReflectUtils.eraseType(elementType)}
        );
        this.buildIndexThreshold = buildIndexThreshold;
        this.elementAsChild = elementAsChild;
        addAll(data);
    }

    @NoProxy
    public void initialize(Collection<T> data) {
        for (T datum : data) {
            addLast0(datum);
        }
    }

    public void setElementAsChild(boolean elementAsChild) {
        this.elementAsChild = elementAsChild;
    }

    public void initId(long id) {
        NncUtils.requireNull(this.id, "id already initialized");
        this.id = id;
    }

    public boolean isElementAsChild() {
        return elementAsChild;
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

    @SuppressWarnings("unused")
    public <K> void buildIndex(IndexMapper<T, K> keyMapper) {
        beforeAccess();
        IndexDesc<T> indexDesc = IndexDesc.create(keyMapper);
        buildIndex(indexDesc);
    }

    public void buildIndex(IndexDesc<T> indexDesc) {
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

    private Map<Object, LinkedList<Node<T>>>  buildIndex0(IndexDesc<T> indexDesc) {
        Map<Object, LinkedList<Node<T>>> index = new HashMap<>();
        forEachNode(
                node -> index.computeIfAbsent(
                        indexDesc.map(node.getValue()),
                        k->new LinkedList<>()
                ).add(node)
        );
        indexes.put(indexDesc, index);
        return index;
    }

    @NoProxy
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
        Table<T> merged = new Table<>(genericType, List.of(), buildIndexThreshold, elementAsChild);
        indexes.keySet().forEach(merged::buildIndex);
        merged.addAll(this);
        merged.addAll(that);
        return merged;
    }

    public <R> Table<R> map(Function<T, R> mapper) {
        return mapAndFilter(mapper, t -> true);
    }

    public <R> Table<R> mapAndFilter(Function<T, R> mapper, Predicate<R> filter) {
        beforeAccess();
        Table<R> that = new Table<>(genericType, List.of(), buildIndexThreshold, elementAsChild);
        for (T t : this) {
            R r = mapper.apply(t);
            if(filter.test(r)) {
                that.add(r);
            }
        }
        return that;
    }

    @SuppressWarnings("unused")
    private Table<T> createSame() {
        Table<T> that = new Table<>(genericType, List.of(), buildIndexThreshold, elementAsChild);
        for (IndexDesc<T> indexDesc : indexes.keySet()) {
            that.buildIndex(indexDesc);
        }
        return that;
    }

    private record IndexDesc<T>(
            String implClass,
            String implMethodName,
            List<Object> capturedArgs,
            IndexMapper<T, ?> indexMapper
    ) {

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

    @SuppressWarnings("unused")
    public ModelIdentity getIdentifier() {
        return identifier;
    }

    @SuppressWarnings("unused")
    public void setIdentifier(ModelIdentity identifier) {
        this.identifier = identifier;
    }

    @SuppressWarnings("unused")
    public String getIdentifierName() {
        return NncUtils.get(identifier, ModelIdentity::name);
    }

    public interface IndexMapper<T, K> extends Function<T, K>, Serializable {

    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        return Map.of(Table.class.getTypeParameters()[0], elementType);
    }

    @Override
    public void clearId() {
        this.id = null;
    }
}
