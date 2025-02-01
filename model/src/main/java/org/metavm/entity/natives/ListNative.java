package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ListNative extends IterableNative {

    public static final Logger logger = LoggerFactory.getLogger(ListNative.class);

    private final ClassInstance instance;
    private final FieldRef arrayField;
    private ArrayInstance array;

    public ListNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = Objects.requireNonNull(instance.getInstanceType().findFieldByName("array"));
        if(instance.isFieldInitialized(arrayField.getRawField())) {
            array = instance.getField(arrayField.getRawField()).resolveArray();
        }
    }

    public Value List(CallContext callContext) {
        return List();
    }

    public Value List() {
        array = new ArrayInstance((ArrayType) arrayField.getPropertyType());
        instance.initField(arrayField.getRawField(), array.getReference());
        return instance.getReference();
    }

    public Value List(Value c, CallContext callContext) {
        if(c instanceof Reference collection) {
            var thatArrayField = collection.resolveObject().getInstanceKlass().getFieldByName("array");
            var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
            array = new ArrayInstance((ArrayType) arrayField.getPropertyType(),
                    new InstanceParentRef(instance.getReference(), arrayField.getRawField()));
            instance.initField(arrayField.getRawField(), array.getReference());
            array.addAll(thatArray);
            return instance.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value ChildList(CallContext callContext) {
        return List(callContext);
    }

    public Value ChildList(Value c, CallContext callContext) {
        return List(c, callContext);
    }

    public Value ArrayList(CallContext callContext) {
        return List(callContext);
    }

    public Value ArrayList__Collection(Value c, CallContext callContext) {
        return List(c, callContext);
    }

    public Value ValueList(CallContext callContext) {
        return List(callContext);
    }

    public Value ValueList(Value c, CallContext callContext) {
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
        array.forEach(action);
    }

    @Override
    public @NotNull Iterator<Value> iterator() {
        return array.iterator();
    }

    public Value getFirst(CallContext callContext) {
        return array.getFirst();
    }

    public Value getLast(CallContext callContext) {
        return array.getLast();
    }

    public Value get(Value index, CallContext callContext) {
        return array.get(getInt(index));
    }

    public Value set(Value index, Value value, CallContext callContext) {
        return array.setElement(getInt(index), value);
    }

    public Value remove(Value instance, CallContext callContext) {
        var it = array.iterator();
        while (it.hasNext()) {
            if (Instances.equals(it.next(), instance, callContext)) {
                it.remove();
                return Instances.one();
            }
        }
        return Instances.zero();
    }

    public Value removeAt(Value index, CallContext callContext) {
        return array.remove(getInt(index));
    }

    public Value contains(Value value, CallContext callContext) {
        for (Value e : array) {
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
        array.clear();
    }

    public Value add(Value instance, CallContext callContext) {
        return add(instance);
    }

    public Value add(Value instance) {
        array.addElement(instance);
        return Instances.one();
    }

    public Value addAll(Value c, CallContext callContext) {
        if(c instanceof Reference collection && collection.resolveObject().isList()) {
            var thatArrayField = collection.resolveObject().getInstanceKlass().getFieldByName("array");
            var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
            array.addAll(thatArray);
            return Instances.one();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public static Value of(Klass klass, Value values, CallContext callContext) {
        if(values instanceof Reference r) {
            var list = ClassInstance.allocate(klass.getType());
            var listNative = (ListNative) NativeMethods.getNativeObject(list);
            listNative.List(callContext);
            r.resolveArray().forEach(e -> listNative.add(e, callContext));
            return list.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(array.isEmpty());
    }

    public Value size(CallContext callContext) {
        return Instances.intInstance(array.size());
    }

    public Value sort(CallContext callContext) {
        array.sort((e1, e2) -> Instances.compare(e1, e2, callContext));
        return Instances.nullInstance();
    }

    public Value removeIf(Value filter, CallContext callContext) {
        if(filter instanceof Reference r) {
            var method = r.resolveObject().getInstanceType().getMethods().getFirst();
            return Instances.intInstance(array.removeIf(e -> method.execute(
                    r, List.of(e), callContext).booleanRet()));
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value hashCode(CallContext callContext) {
        int h = 0;
        for (Value value : array) {
            h = 31 * h + Instances.hashCode(value, callContext);
        }
        return Instances.intInstance(h);
    }

    public Value equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.get() instanceof ClassInstance that
                    && Objects.equals(that.getInstanceType().asSuper(StdKlass.list.get()), instance.getInstanceType().asSuper(StdKlass.list.get()))) {
                var thatNat = new ListNative(that);
                var thatArray = thatNat.toArray();
                if(array.size() == thatArray.size()) {
                    var i = 0;
                    for (Value value : array) {
                        if(!Instances.equals(value, thatArray.get(i++), callContext))
                            return Instances.zero();
                    }
                    return Instances.one();
                }
            }
        }
        return Instances.zero();
    }

    public ArrayInstance toArray() {
        return array;
    }

    public Value toArray(CallContext callContext) {
        return array.copy().getReference();
    }

    @Override
    public Value forEach(Value action, CallContext callContext) {
        if(action instanceof Reference r) {
            var method = r.resolveObject().getInstanceType().getMethods().getFirst();
            array.forEach(e -> method.execute(r, List.of(e), callContext));
            return Instances.nullInstance();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public ClassInstance getInstance() {
        return instance;
    }

    public void reverse() {
        array.reverse();
    }

    public Value sort(Value comparator, CallContext callContext) {
        if(comparator.isNull())
            sort(callContext);
        else {
            var l = comparator.resolveObject();
            var compareMethod = l.getInstanceType().getMethod(StdMethod.comparatorCompare.get());
            array.sort((e1, e2) -> Instances.toInt(Flows.invokeVirtual(compareMethod, l, List.of(e1, e2), callContext)));
        }
        return Instances.nullInstance();
    }

}
