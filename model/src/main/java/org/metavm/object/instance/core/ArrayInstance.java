package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.NoProxy;
import org.metavm.object.instance.ArrayListener;
import org.metavm.object.instance.rest.ArrayInstanceParam;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArrayInstance extends Instance implements Iterable<Value> {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Value> elements = new ArrayList<>();
    private final transient List<ArrayListener> listeners = new ArrayList<>();

    public ArrayInstance(ArrayType type) {
        this(null, type, type.isEphemeral(), null);
    }

    public ArrayInstance(Id id, ArrayType type, boolean ephemeral, @Nullable Consumer<Instance> load) {
        super(id, type, 0, 0, ephemeral, load);
    }

    public ArrayInstance(ArrayType type, List<? extends Value> elements) {
        super(type);
        this.addAll(elements);
    }

    public ArrayInstance(ArrayType type, @Nullable InstanceParentRef parentRef) {
        super(type);
        setParentRef(parentRef);
    }

    public ArrayInstance(ArrayType type, @Nullable InstanceParentRef parentRef, boolean ephemeral) {
        super(null, type, 0L, 0L, ephemeral, null);
        setParentRef(parentRef);
    }

    @NoProxy
    @Override
    public void setType(Type type) {
        if(type instanceof ArrayType)
            super.setType(type);
        else
            throw new IllegalArgumentException(type + " is not an array type");
    }

    @NoProxy
    public void reset(List<Value> elements) {
        clearInternal();
        for (Value element : elements)
            addInternally(element);
        if (!isNew() && !isLoaded())
            setLoaded(false);
    }

    private void clearInternal() {
        for (Value instance : new ArrayList<>(elements)) {
            removeElement(instance);
        }
    }

    @NoProxy
    public boolean isChildArray() {
        return getType().getKind() == ArrayKind.CHILD;
    }

    public Set<Instance> getChildren() {
        ensureLoaded();
        if (getType().getKind() == ArrayKind.CHILD) {
            return NncUtils.filterAndMapUnique(elements, Value::isNotNull, Value::resolveDurable);
        } else {
            return Set.of();
        }
    }

    @Override
    protected void writeBody(InstanceOutput output) {
        ensureLoaded();
        var elements = this.elements;
        int size = 0;
        for (Value element : elements) {
            if (element.isNull() || !element.shouldSkipWrite())
                size++;
        }
        output.writeInt(size);
        if (isChildArray()) {
            for (Value element : elements) {
                if (element.isNull() || !element.shouldSkipWrite())
                    output.writeInstance(element);
            }
        } else {
            for (Value element : elements) {
                if (element.isNull() || !element.shouldSkipWrite()) {
                    output.writeValue(element);
                }
            }
        }
    }

    @Override
    @NoProxy
    protected void readFrom(InstanceInput input) {
        var parentField = getParentField();
        var elements = this.elements;
        int len = input.readInt();
        input.setParentField(null);
        for (int i = 0; i < len; i++) {
            var element = input.readValue();
            elements.add(element);
        }
        input.setParentField(parentField);
    }

    public Value get(int index) {
        ensureLoaded();
        checkIndex(index);
        return elements.get(index);
    }

    public Value getInstance(int i) {
        ensureLoaded();
        return elements.get(i);
    }

    public int size() {
        ensureLoaded();
        return elements.size();
    }

    public boolean isEmpty() {
        ensureLoaded();
        return elements.isEmpty();
    }

    public boolean contains(Object o) {
        ensureLoaded();
        //noinspection SuspiciousMethodCalls
        return elements.contains(o);
    }

    public BooleanValue instanceContains(Value instance) {
        ensureLoaded();
        return Instances.createBoolean(contains(instance));
    }

    @NotNull
    public Iterator<Value> iterator() {
        ensureLoaded();
        return elements.iterator();
    }

    public ListIterator<Value> listIterator() {
        ensureLoaded();
        return elements.listIterator();
    }

    public boolean addElement(Value element) {
        ensureLoaded();
        return addInternally(element);
    }

    /**
     * For instance copy only
     */
    void setElementDirectly(int index, Value instance) {
        this.elements.set(index, instance);
    }

    void removeChild(Value element) {
        ensureLoaded();
        removeInternally(element);
    }

    public Value removeElement(int index) {
        ensureLoaded();
        checkIndex(index);
        var removed = elements.remove(index);
        onRemove(removed);
        return removed;
    }

    public Value setElement(int index, Value element) {
        ensureLoaded();
        checkIndex(index);
        element = checkElement(element);
        if (isChildArray() && element.isNotNull())
            element.resolveDurable().setParent(this, null);
        var removed = elements.set(index, element);
        if (removed != null)
            onRemove(removed);
        onAdd(element);
        return removed;
    }

    private Value checkElement(Value element) {
        var elementType = getType().getElementType();
        if (elementType.isAssignableFrom(element.getType()))
            return element;
        else
            return element.convert(elementType);
    }

    private void checkIndex(int index) {
        AssertUtils.assertTrue(index >= 0 && index < elements.size(), ErrorCode.INDEX_OUT_OF_BOUND);
    }

    private boolean addInternally(Value element) {
        element = checkElement(element);
        if (isChildArray() && element.isNotNull())
            element.resolveDurable().setParent(this, null);
        elements.add(element);
        onAdd(element);
        return true;
    }

    public void setElements(List<Value> elements) {
        ensureLoaded();
        for (Value element : new ArrayList<>(this.elements))
            removeElement(element);
        this.addAll(elements);
    }

    public boolean removeElement(Object element) {
        ensureLoaded();
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
        ensureLoaded();
        c.forEach(this::addElement);
        return true;
    }

    public int length() {
        ensureLoaded();
        return elements.size();
    }

    public List<Value> getElements() {
        ensureLoaded();
        return elements;
    }

    public Value getElement(int index) {
        ensureLoaded();
        return elements.get(index);
    }

    @Override
    public Set<Instance> getRefInstances() {
        ensureLoaded();
        return new IdentitySet<>(
                NncUtils.filterByType(elements, Instance.class)
        );
    }

//    @Override
    @NoProxy
    public boolean isReference() {
        return true;
    }

    @Override
    public String getTitle() {
        ensureLoaded();
        List<Value> first = elements.subList(0, Math.min(3, elements.size()));
        return NncUtils.join(first, Value::getTitle, ",") + (elements.size() > 3 ? "..." : "");
    }

    @Override
    public void forEachChild(Consumer<Instance> action) {
        if(isChildArray()) {
            elements.forEach(e -> {
                if (e instanceof Reference r)
                    action.accept(r.resolve());
            });
        }
    }

    @Override
    public void forEachMember(Consumer<Instance> action) {
        if(isChildArray()) {
            elements.forEach(e -> {
                if (e instanceof Reference r)
                    action.accept(r.resolve());
            });
        }
        else {
            elements.forEach(e -> {
                if(e instanceof Reference r && r.isValueReference())
                    action.accept(r.resolve());
            });
        }
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
        boolean isChild = isChildArray();
        elements.forEach(e -> {
            if(e instanceof Reference r)
                action.accept(r, isChild);
        });
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        var isChild = isChildArray();
        var elementType = getType().getElementType();
        elements.forEach(e -> {
            if(e instanceof Reference r)
                action.accept(r, isChild, elementType);
        });
    }

    @Override
    public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {
        var isChild = isChildArray();
        var elementType = getType().getElementType();
        var it = elements.listIterator();
        while (it.hasNext()) {
            var v = it.next();
            if(v instanceof Reference r) {
                var r1 = function.apply(r, isChild, elementType);
                if(r1 != r)
                    it.set(r1);
            }
        }
    }

    //    @Override
    protected ArrayInstanceParam getParam() {
        ensureLoaded();
        if (isChildArray()) {
            return new ArrayInstanceParam(
                    true,
                    NncUtils.map(elements, e ->
                            new InstanceFieldValue(
                                    e.getTitle(), e.toDTO()
                            )
                    )
            );
        } else {
            return new ArrayInstanceParam(
                    false,
                    NncUtils.map(elements, Value::toFieldValueDTO)
            );
        }
    }

//    @Override
//    @NoProxy
//    public <R> R accept(InstanceVisitor<R> visitor) {
//        return visitor.visitArrayInstance(this);
//    }

//    @Override
    public <R> void acceptReferences(ValueVisitor<R> visitor) {
        ensureLoaded();
        elements.forEach(visitor::visit);
    }

//    @Override
    public <R> void acceptChildren(ValueVisitor<R> visitor) {
        ensureLoaded();
        if (isChildArray())
            elements.forEach(visitor::visit);
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
    public BooleanValue __remove__(Value instance) {
        return Instances.booleanInstance(removeElement(instance));
    }

    @SuppressWarnings("unused")
    public Value __removeAt__(Value index) {
        return removeElement(getIndex(index));
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
    public LongValue __size__() {
        return Instances.longInstance(size());
    }

    @Override
    public ArrayType getType() {
        ensureLoaded();
        return (ArrayType) super.getType();
    }

//    @Override
    public InstanceFieldValue toFieldValueDTO() {
        ensureLoaded();
        return new InstanceFieldValue(getTitle(), toDTO());
    }

    public void clear() {
        ensureLoaded();
        clearInternal();
    }

    public void addListener(ArrayListener listener) {
        listeners.add(listener);
    }

    public boolean removeIf(Predicate<Value> filter) {
        ensureLoaded();
        return elements.removeIf(filter);
    }

//    @Override
    protected void writeTree(TreeWriter treeWriter) {
        ensureLoaded();
        treeWriter.writeLine(getType().getName());
        treeWriter.indent();
        if(isChildArray()) {
            for (Value element : elements) {
                if(element instanceof Reference r)
                    r.resolve().writeTree(treeWriter);
                else
                    treeWriter.writeLine(element.getTitle());
            }
        }
        else {
            for (Value element : elements) {
                if(element instanceof Reference r && r.isValueReference())
                    r.resolve().writeTree(treeWriter);
                else
                    treeWriter.writeLine(element.getTitle());
            }
        }
        treeWriter.deIndent();
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitArrayInstance(this);
    }

    public List<Object> toJson(IEntityContext context) {
        var list = new ArrayList<>();
        forEach(e -> list.add(e.toJson(context)));
        return list;
    }

//    @Override
    public boolean isMutable() {
        return getType().getKind() != ArrayKind.VALUE;
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
    public ArrayInstance copy() {
        var copy = new ArrayInstance(getType());
        if(isChildArray()) {
            var copyElements = copy.elements;
            elements.forEach(e -> {
                if (e instanceof Reference r)
                    e = r.resolve().copy().getReference();
                copyElements.add(e);
            });
        }
        else
            copy.elements.addAll(elements);
        return copy;
    }

    public void reverse() {
        Collections.reverse(elements);
    }

    public ArrayInstance copyOfRange(int from, int to) {
        return copyOfRange(from, to, getType());
    }

    public ArrayInstance copyOfRange(int from, int to, ArrayType type) {
        return new ArrayInstance(type, elements.subList(from, to));
    }

    public ArrayInstance copyOf(int newLength) {
        return copyOf(newLength, getType());
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

}
