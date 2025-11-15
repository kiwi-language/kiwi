package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.ArrayListener;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.util.*;

import java.util.*;
import java.util.function.*;

public class ArrayInstance extends MvInstance implements Iterable<Value> {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Value> elements = new ArrayList<>();
    private final transient List<ArrayListener> listeners = new ArrayList<>();

    public ArrayInstance(ArrayType type) {
        this(null, type, type.isEphemeral());
    }

    public ArrayInstance(Id id, ArrayType type, boolean ephemeral) {
        super(id, type, 0, 0, ephemeral, false);
    }

    public ArrayInstance(ArrayType type, List<? extends Value> elements) {
        super(type, false);
        this.addAll(elements);
    }

    public void reset(List<Value> elements) {
        clearInternal();
        for (Value element : elements)
            addInternally(element);
    }

    private void clearInternal() {
        for (Value instance : new ArrayList<>(elements)) {
            remove(instance);
        }
    }

    @Override
    protected void writeBody(MvOutput output) {
        var elements = this.elements;
        int size = 0;
        for (Value element : elements) {
            if (element.isNull() || !element.shouldSkipWrite())
                size++;
        }
        output.writeInt(size);
        for (Value element : elements) {
            if (element.isNull() || !element.shouldSkipWrite()) {
                output.writeValue(element);
            }
        }
    }

    @Override
    protected void readBody(InstanceInput input) {
        var elements = this.elements;
        int len = input.readInt();
        for (int i = 0; i < len; i++) {
            var element = input.readValue();
            elements.add(element);
        }
    }

    public Value get(int index) {
        checkIndex(index);
        return elements.get(index);
    }

    public Value getInstance(int i) {
        return elements.get(i);
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public boolean contains(Object o) {
        //noinspection SuspiciousMethodCalls
        return elements.contains(o);
    }

    public BooleanValue instanceContains(Value instance) {
        return Instances.createBoolean(contains(instance));
    }

    @NotNull
    public Iterator<Value> iterator() {
        return elements.iterator();
    }

    public ListIterator<Value> listIterator() {
        return elements.listIterator();
    }

    public boolean addElement(Value element) {
        return addInternally(element);
    }

    /**
     * For instance copy only
     */
    void setElementDirectly(int index, Value instance) {
        this.elements.set(index, instance);
    }

    public Value remove(int index) {
        checkIndex(index);
        var removed = elements.remove(index);
        onRemove(removed);
        return removed;
    }

    public Value setElement(int index, Value element) {
        checkIndex(index);
        checkElement(element);
        var removed = elements.set(index, element);
        if (removed != null)
            onRemove(removed);
        onAdd(element);
        return removed;
    }

    private void checkElement(Value element) {
        if (!getInstanceType().getElementType().isAssignableFrom(element.getValueType()))
            throw new BusinessException(ErrorCode.INCORRECT_ARRAY_ELEMENT, element, getInstanceType());
    }

    private void checkIndex(int index) {
        AssertUtils.assertTrue(index >= 0 && index < elements.size(), ErrorCode.INDEX_OUT_OF_BOUND);
    }

    private boolean addInternally(Value element) {
        checkElement(element);
        elements.add(element);
        onAdd(element);
        return true;
    }

    public void setElements(List<Value> elements) {
        for (Value element : new ArrayList<>(this.elements))
            remove(element);
        this.addAll(elements);
    }

    public boolean remove(Object element) {
        return removeInternally(element);
    }

    private boolean removeInternally(Object element) {
        //noinspection SuspiciousMethodCalls
        if (elements.remove(element)) {
            Value removed = (Value) element;
            onRemove(removed);
            return true;
        }
        return false;
    }

    private void onRemove(Value instance) {
        setModified();
        for (ArrayListener listener : listeners)
            listener.onRemove(instance);
    }

    private void onAdd(Value instance) {
        setModified();
        for (ArrayListener listener : listeners)
            listener.onAdd(instance);
    }

    public boolean addAll(@NotNull Collection<? extends Value> c) {
        return addAll((Iterable<? extends Value>) c);
    }

    public boolean addAll(Iterable<? extends Value> c) {
        c.forEach(this::addElement);
        return true;
    }

    public int length() {
        return elements.size();
    }

    public List<Value> getElements() {
        return elements;
    }

    public Value getElement(int index) {
        return elements.get(index);
    }

    //    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public String getTitle() {
        return getStringId();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public void forEachValue(Consumer<? super Instance> action) {
        elements.forEach(e -> {
            if(e instanceof ValueReference vr)
                action.accept(vr.get());
        });
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        elements.forEach(e -> {
            if(e instanceof Reference r)
                action.accept(r);
        });
    }

    @Override
    public void forEachReference(BiConsumer<Reference, Boolean> action) {
        elements.forEach(e -> {
            if(e instanceof Reference r)
                action.accept(r, false);
        });
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        var elementType = getInstanceType().getElementType();
        elements.forEach(e -> {
            if(e instanceof Reference r)
                action.accept(r, false, elementType);
        });
    }

    @Override
    public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {
        var elementType = getInstanceType().getElementType();
        var it = elements.listIterator();
        while (it.hasNext()) {
            var v = it.next();
            if(v instanceof Reference r) {
                var r1 = function.apply(r, false, elementType);
                if(r1 != r)
                    it.set(r1);
            }
        }
    }

    //    @Override
    public <R> void acceptReferences(ValueVisitor<R> visitor) {
        elements.forEach(visitor::visit);
    }

//    @Override
    public <R> void acceptChildren(ValueVisitor<R> visitor) {
    }

    @SuppressWarnings("unused")
    public ArrayInstance __init__() {
        return this;
    }

    @SuppressWarnings("unused")
    public Value __get__(Value index) {
        return get(getIndex(index));
    }

    @SuppressWarnings("unused")
    public Value __set__(Value index, Value value) {
        return setElement(getIndex(index), value);
    }

    @SuppressWarnings("unused")
    public Value __remove__(Value instance) {
        return Instances.intInstance(remove(instance));
    }

    @SuppressWarnings("unused")
    public Value __removeAt__(Value index) {
        return remove(getIndex(index));
    }

    private int getIndex(Value instance) {
        if (instance instanceof LongValue longInstance) {
            return longInstance.getValue().intValue();
        } else {
            throw new InternalException("Index must be a LongInstance, actually got: " + instance);
        }
    }

    @SuppressWarnings("unused")
    public void __clear__() {
        clear();
    }

    @SuppressWarnings("unused")
    public void __add__(Value instance) {
        addElement(instance);
    }

    @SuppressWarnings("unused")
    public IntValue __size__() {
        return Instances.intInstance(size());
    }

    @Override
    public ArrayType getInstanceType() {
        return (ArrayType) super.getInstanceType();
    }

    public void clear() {
        clearInternal();
    }

    public void addListener(ArrayListener listener) {
        listeners.add(listener);
    }

    public boolean removeIf(Predicate<Value> filter) {
        return elements.removeIf(filter);
    }

//    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.writeLine(getInstanceType().getName() + " " + tryGetId());
        treeWriter.indent();
        for (Value element : elements) {
            if(element instanceof ValueReference vr)
                vr.get().writeTree(treeWriter);
            else
                treeWriter.writeLine(element.getTitle());
        }
        treeWriter.deIndent();
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitArrayInstance(this);
    }

    public List<Object> toJson(IInstanceContext context) {
        var list = new ArrayList<>();
        forEach(e -> list.add(e.toJson()));
        return list;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public void sort(Comparator<Value> comparator) {
        elements.sort(comparator);
    }

    public void sort(int from, int to, Comparator<Value> comparator) {
        elements.subList(from, to).sort(comparator);
    }

    @Override
    public ArrayInstance copy(Function<ClassType, Id> idSupplier) {
        var copy = new ArrayInstance(getInstanceType());
        copy.elements.addAll(elements);
        return copy;
    }

    public void reverse() {
        Collections.reverse(elements);
    }

    public ArrayInstance copyOfRange(int from, int to) {
        return copyOfRange(from, to, getInstanceType());
    }

    public ArrayInstance copyOfRange(int from, int to, ArrayType type) {
        return new ArrayInstance(type, elements.subList(from, to));
    }

    public ArrayInstance copyOf(int newLength) {
        return copyOf(newLength, getInstanceType());
    }

    public ArrayInstance copyOf(int newLength, ArrayType type) {
        int sz = size();
        var copy = new ArrayInstance(type, elements.subList(0, Math.min(sz, newLength)));
        int m = newLength - sz;
        for (int i = 0; i < m; i++) {
            copy.addElement(Instances.getDefaultValue(type.getElementType()));
        }
        return copy;
    }

    public Value getFirst() {
        return get(0);
    }

    public Value getLast() {
        return get(size() - 1);
    }
}
