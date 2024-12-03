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
import org.metavm.util.NncUtils;
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
        arrayField = NncUtils.requireNonNull(instance.getType().findFieldByName("array"));
        if(instance.isFieldInitialized(arrayField.getRawField())) {
            array = instance.getField(arrayField.getRawField()).resolveArray();
        }
    }

    public Value List(CallContext callContext) {
        return List();
    }

    public Value List() {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField.getRawField(), array.getReference());
        return instance.getReference();
    }

    public Value List(Value c, CallContext callContext) {
        if(c instanceof Reference collection) {
            var thatArrayField = collection.resolveObject().getKlass().getFieldByName("array");
            var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
            array = new ArrayInstance((ArrayType) arrayField.getType(),
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

    public Value ArrayList(Value c, CallContext callContext) {
        return List(c, callContext);
    }

    public Value ValueList(CallContext callContext) {
        return List(callContext);
    }

    public Value ValueList(Value c, CallContext callContext) {
        return List(c, callContext);
    }

    public Reference iterator(CallContext callContext) {
        var iteratorImplType = ClassType.create(StdKlass.iteratorImpl.get(), List.of(instance.getType().getFirstTypeArgument()));
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

    public Value get(Value index, CallContext callContext) {
        return array.get(getInt(index));
    }

    public Value set(Value index, Value value, CallContext callContext) {
        return array.setElement(getInt(index), value);
    }

    public BooleanValue remove(Value instance, CallContext callContext) {
        return Instances.booleanInstance(array.removeElement(instance));
    }

    public Value removeAt(Value index, CallContext callContext) {
        return array.removeElement(getInt(index));
    }

    public Value contains(Value value, CallContext callContext) {
        return Instances.booleanInstance(array.contains(value));
    }

    public void clear(CallContext callContext) {
        clear();
    }

    public void clear() {
        array.clear();
    }

    public BooleanValue add(Value instance, CallContext callContext) {
        return add(instance);
    }

    public BooleanValue add(Value instance) {
        array.addElement(instance);
        return Instances.trueInstance();
    }

    public BooleanValue addAll(Value c, CallContext callContext) {
        if(c instanceof Reference collection && collection.resolveObject().isList()) {
            var thatArrayField = collection.resolveObject().getKlass().getFieldByName("array");
            var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
            array.addAll(thatArray);
            return Instances.trueInstance();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public static Reference of(Klass klass, Value values, CallContext callContext) {
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
        return Instances.booleanInstance(array.isEmpty());
    }

    public IntValue size(CallContext callContext) {
        return Instances.intInstance(array.size());
    }

    public void sort(CallContext callContext) {
        array.sort((e1, e2) -> Instances.compare(e1, e2, callContext));
    }

    public BooleanValue removeIf(Value filter, CallContext callContext) {
        if(filter instanceof Reference r) {
            var method = r.resolveObject().getType().getMethods().get(0);
            return Instances.booleanInstance(array.removeIf(e -> method.execute(
                    r.resolveObject(), List.of(e), callContext).booleanRet()));
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public IntValue hashCode(CallContext callContext) {
        int h = 0;
        for (Value value : array) {
            h = 31 * h + Instances.hashCode(value, callContext);
        }
        return Instances.intInstance(h);
    }

    public BooleanValue equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.resolve() instanceof ClassInstance that
                    && Objects.equals(that.getType().findAncestorByKlass(StdKlass.list.get()), instance.getType().findAncestorByKlass(StdKlass.list.get()))) {
                var thatNat = new ListNative(that);
                var thatArray = thatNat.toArray();
                if(array.size() == thatArray.size()) {
                    var i = 0;
                    for (Value value : array) {
                        if(!Instances.equals(value, thatArray.get(i++), callContext))
                            return Instances.falseInstance();
                    }
                    return Instances.trueInstance();
                }
            }
        }
        return Instances.falseInstance();
    }

    public ArrayInstance toArray() {
        return array;
    }

    public Reference toArray(CallContext callContext) {
        return array.copy().getReference();
    }

    @Override
    public void forEach(Value action, CallContext callContext) {
        if(action instanceof Reference r) {
            var method = r.resolveObject().getType().getMethods().get(0);
            array.forEach(e -> method.execute(r.resolveObject(), List.of(e), callContext));
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

    public void sort(Value comparator, CallContext callContext) {
        if(comparator.isNull())
            sort(callContext);
        else {
            var l = comparator.resolveObject();
            var compareMethod = l.getType().getMethod(StdMethod.comparatorCompare.get());
            array.sort((e1, e2) -> Instances.toInt(Flows.invokeVirtual(compareMethod, l, List.of(e1, e2), callContext)));
        }
    }

}
