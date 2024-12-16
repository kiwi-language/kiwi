package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Consumer;

public class HashSetNative extends SetNative {

    private final ClassInstance instance;
    private final Set<HashKeyWrap> set = new HashSet<>();
    private final FieldRef arrayField;
    private ArrayInstance array;

    public HashSetNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getType().findFieldByName("array"));
        if (instance.isFieldInitialized(arrayField.getRawField())) {
            var instCtx = Objects.requireNonNull(instance.getContext(), "InstanceContext is missing in " + instance);
            array = instance.getField(arrayField.getRawField()).resolveArray();
            initializeElementToIndex(instCtx);
        }
    }

    public Value HashSet(CallContext callContext) {
        array = new ArrayInstance((ArrayType) arrayField.getType());
        instance.initField(arrayField.getRawField(), array.getReference());
        return instance.getReference();
    }

    public Value HashSet__Collection(Value c, CallContext callContext) {
        var collection = (Reference) c;
        var thatArrayField = collection.resolveObject().getKlass().getFieldByName("array");
        var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
        array = new ArrayInstance((ArrayType) arrayField.getType(),
                new InstanceParentRef(instance.getReference(), arrayField.getRawField()));
        instance.initField(arrayField.getRawField(), array.getReference());
        array.addAll(thatArray);
        initializeElementToIndex(callContext);
        return instance.getReference();
    }

    public Reference iterator(CallContext callContext) {
        var iteratorImplType = KlassType.create(StdKlass.iteratorImpl.get(), List.of(instance.getType().getFirstTypeArgument()));
        var it = ClassInstance.allocate(iteratorImplType);
        var itNative = (IteratorImplNative) NativeMethods.getNativeObject(it);
        itNative.IteratorImpl(instance, callContext);
        return it.getReference();
    }

    @Override
    public void forEach(Consumer<? super Value> action) {
        set.forEach(k -> action.accept(k.value()));
    }

    @Override
    public @NotNull Iterator<Value> iterator() {
        return NncUtils.mapIterator(set.iterator(), HashKeyWrap::value);
    }

    public Value add(Value value, CallContext callContext) {
        var keyWrap = new HashKeyWrap(value, callContext);
        return Instances.intInstance(set.add(keyWrap));
    }

    public Value remove(Value value, CallContext callContext) {
        var keyWrap = new HashKeyWrap(value, callContext);
        return Instances.intInstance(set.remove(keyWrap));
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(set.isEmpty());
    }

    public boolean contains0(Value value, CallContext callContext) {
        return set.contains(new HashKeyWrap(value, callContext));
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public ClassInstance getInstance() {
        return instance;
    }

    public void clear(CallContext callContext) {
        array.clear();
    }

    @Override
    public void forEach(Value action, CallContext callContext) {
        if(action instanceof FunctionValue functionValue)
            array.forEach(e -> functionValue.execute(List.of(e), callContext));
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    private void initializeElementToIndex(CallContext callContext) {
        for (Value element : array) {
            set.add(new HashKeyWrap(element, callContext));
        }
    }

    @Override
    public void flush() {
        array.setElements(NncUtils.map(set, HashKeyWrap::value));
    }
}
