package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ArrayListNative extends AbstractListNative implements ListNative, StatefulNative {

    public static final Logger logger = LoggerFactory.getLogger(ArrayListNative.class);

    protected final ClassInstance instance;
    private final Type elementType;
    protected List<Value> list = new ArrayList<>();

    public ArrayListNative(ClassInstance instance) {
        this.instance = instance;
        ((MvClassInstance) instance).setNativeObject(this);
        elementType = instance.getInstanceType().getTypeArguments().getFirst();
    }

    public Value List(CallContext callContext) {
        return List();
    }

    public Value List() {
        return instance.getReference();
    }

    public Value List(Value c, CallContext callContext) {
        if(c instanceof Reference collection) {
            var thatNative = (IterableNative) ((MvClassInstance) collection.resolveDurable()).getNativeObject();
            thatNative.forEach(this::add0);
            return instance.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value ArrayList(CallContext callContext) {
        return List(callContext);
    }

    public Value ArrayList__Collection(Value c, CallContext callContext) {
        return List(c, callContext);
    }

    public Value iterator(CallContext callContext) {
        var iteratorImplType = KlassType.create(StdKlass.iteratorImpl.get(), List.of(instance.getInstanceType().getFirstTypeArgument()));
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it.getReference();
    }

    @Override
    public void forEach(Consumer<? super Value> action) {
        list.forEach(action);
    }

    @Override
    public @NotNull Iterator<Value> iterator() {
        return list.iterator();
    }

    @Override
    public Value getFirst(CallContext callContext) {
        return list.getFirst();
    }

    @Override
    public Value getLast(CallContext callContext) {
        return list.getLast();
    }

    public Value get(Value index, CallContext callContext) {
        return list.get(getInt(index));
    }

    public Value set(Value index, Value value, CallContext callContext) {
        return list.set(getInt(index), value);
    }

    public Value remove(Value instance, CallContext callContext) {
        var it = list.iterator();
        while (it.hasNext()) {
            if (Instances.equals(it.next(), instance, callContext)) {
                it.remove();
                return Instances.one();
            }
        }
        return Instances.zero();
    }

    public Value removeAt(Value index, CallContext callContext) {
        return list.remove(getInt(index));
    }

    public Value contains(Value value, CallContext callContext) {
        for (Value e : list) {
            if (Instances.equals(e, value, callContext))
                return Instances.one();
        }
        return Instances.zero();
    }

    public Value clear(CallContext callContext) {
        clear();
        return Instances.nullInstance();
    }

    public void clear() {
        list.clear();
    }

    public Value add(Value instance, CallContext callContext) {
        return add(instance);
    }

    public Value add(Value instance) {
        add0(instance);
        return Instances.one();
    }

    public Value addAll(Value c, CallContext callContext) {
        if(c instanceof Reference collection && collection.resolveObject().isList()) {
            var thatNative = (IterableNative) (collection.resolveMvObject().getNativeObject());
            thatNative.forEach(this::add0);
            return Instances.one();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    protected void add0(Value v) {
        list.add(v);
    }

    public static Value of(Klass klass, Value values, CallContext callContext) {
        if(values instanceof Reference r) {
            var list = ClassInstance.allocate(klass.getType());
            var listNative = (ArrayListNative) NativeMethods.getNativeObject(list);
            listNative.List(callContext);
            r.resolveArray().forEach(e -> listNative.add(e, callContext));
            return list.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(list.isEmpty());
    }

    public Value size(CallContext callContext) {
        return Instances.intInstance(list.size());
    }

    public Value sort(CallContext callContext) {
        list.sort((e1, e2) -> Instances.compare(e1, e2, callContext));
        return Instances.nullInstance();
    }

    public Value removeIf(Value filter, CallContext callContext) {
        if(filter instanceof Reference r) {
            var method = r.resolveObject().getInstanceType().getMethods().getFirst();
            return Instances.intInstance(list.removeIf(e -> method.execute(
                    r, List.of(e), callContext).booleanRet()));
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value hashCode(CallContext callContext) {
        int h = 0;
        for (Value value : list) {
            h = 31 * h + Instances.hashCode(value, callContext);
        }
        return Instances.intInstance(h);
    }

    public Value equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.get() instanceof MvClassInstance that
                    && Objects.equals(that.getInstanceType().asSuper(StdKlass.list.get()), instance.getInstanceType().asSuper(StdKlass.list.get()))) {
                var thatNat = (ArrayListNative) that.getNativeObject();
                var thatList = thatNat.list;
                if(list.size() == thatList.size()) {
                    var i = 0;
                    for (Value value : list) {
                        if(!Instances.equals(value, thatList.get(i++), callContext))
                            return Instances.zero();
                    }
                    return Instances.one();
                }
            }
        }
        return Instances.zero();
    }

    public Value toArray(CallContext callContext) {
        var array = new ArrayInstance(Types.getArrayType(Types.getNullableAnyType()));
        list.forEach(array::addElement);
        return array.getReference();
    }

    @Override
    public Value forEach(Value action, CallContext callContext) {
        if(action instanceof Reference r) {
            var method = r.resolveObject().getInstanceType().getMethods().getFirst();
            list.forEach(e -> method.execute(r, List.of(e), callContext));
            return Instances.nullInstance();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value writeObject(Value o, CallContext callContext) {
        var out = ((MvObjectOutputStream) o.resolveObject()).getOut();
        out.writeInt(list.size());
        for (Value value : list) {
            out.writeValue(value);
        }
        return Instances.nullInstance();
    }

    public Value readObject(Value i, CallContext callContext) {
        var in = ((MvObjectInputStream) i.resolveObject()).getInput();
        var size = in.readInt();
        for (int i1 = 0; i1 < size; i1++) {
            list.add(in.readValue());
        }
        return Instances.nullInstance();
    }

    public ClassInstance getInstance() {
        return instance;
    }

    public void reverse() {
        Collections.reverse(list);
    }

    public Value sort(Value comparator, CallContext callContext) {
        if(comparator.isNull())
            sort(callContext);
        else {
            var l = comparator.resolveObject();
            var compareMethod = l.getInstanceType().getMethod(StdMethod.comparatorCompare.get());
            list.sort((e1, e2) -> Instances.toInt(Flows.invokeVirtual(compareMethod, l, List.of(e1, e2), callContext)));
        }
        return Instances.nullInstance();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (Value value : list) {
            if (value instanceof Reference r) action.accept(r);
        }
    }

    @Override
    public void forEachReference(BiConsumer<Reference, Boolean> action) {
        for (Value value : list) {
            if (value instanceof Reference r) action.accept(r, false);
        }
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        for (Value value : list) {
            if (value instanceof Reference r) action.accept(r, false, elementType);
        }
    }

    @Override
    public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {
        var newList = new ArrayList<Value>();
        for (Value value : list) {
            if (value instanceof Reference r)
                newList.add(function.apply(r, false, elementType));
            else
                newList.add(value);
        }
        list = newList;
    }

    public List<Value> getList() {
        return Collections.unmodifiableList(list);
    }

}
