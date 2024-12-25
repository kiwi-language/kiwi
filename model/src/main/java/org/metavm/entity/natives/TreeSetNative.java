package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class TreeSetNative extends SetNative {

    private final ClassInstance instance;
    private final TreeSet<ComparableKeyWrap> set = new TreeSet<>();
    private final FieldRef arrayField;
    private ArrayInstance array;

    public TreeSetNative(ClassInstance instance) {
        this.instance = instance;
        arrayField = NncUtils.requireNonNull(instance.getType().findFieldByName("array"));
        if (instance.isFieldInitialized(arrayField.getRawField())) {
            var instCtx = Objects.requireNonNull(instance.getContext(), "InstanceContext is missing in " + instance);
            array = instance.getField(arrayField.getRawField()).resolveArray();
            initializeSet(instCtx);
        }
    }

    public Value TreeSet(CallContext callContext) {
        array = new ArrayInstance((ArrayType) arrayField.getPropertyType());
        instance.initField(arrayField.getRawField(), array.getReference());
        return instance.getReference();
    }

    public Value TreeSet(Value c, CallContext callContext) {
        if(c instanceof Reference collection) {
            var thatArrayField = collection.resolveObject().getKlass().getFieldByName("array");
            var thatArray = collection.resolveObject().getField(thatArrayField).resolveArray();
            array = new ArrayInstance((ArrayType) arrayField.getPropertyType(),
                    new InstanceParentRef(instance.getReference(), arrayField.getRawField()));
            instance.initField(arrayField.getRawField(), array.getReference());
            array.addAll(thatArray);
            initializeSet(callContext);
            return instance.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value iterator(CallContext callContext) {
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

    public Value first(CallContext callContext) {
        if(set.isEmpty()) {
            throw new NoSuchElementException();
        }
        return set.first().value();
    }

    public Value add(Value value, CallContext callContext) {
        var keyWrap = new ComparableKeyWrap(value, callContext);
        return Instances.intInstance(set.add(keyWrap));
    }

    public Value remove(Value value, CallContext callContext) {
        var keyWrap = new ComparableKeyWrap(value, callContext);
        return Instances.intInstance(set.remove(keyWrap));
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(set.isEmpty());
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains0(Value value, CallContext callContext) {
        return set.contains(new ComparableKeyWrap(value, callContext));
    }

    public Value clear(CallContext callContext) {
        set.clear();
        return Instances.nullInstance();
    }

    @Override
    public Value forEach(Value action, CallContext callContext) {
        if(action instanceof FunctionValue functionValue) {
            array.forEach(e -> functionValue.execute(List.of(e), callContext));
            return Instances.nullInstance();
        } else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    private void initializeSet(CallContext callContext) {
        for (Value element : array) {
            set.add(new ComparableKeyWrap(element, callContext));
        }
    }

    @Override
    public void flush() {
        array.setElements(NncUtils.map(set, ComparableKeyWrap::value));
    }

    public @NotNull Iterator<Value> iterator() {
        return NncUtils.mapIterator(set.iterator(), ComparableKeyWrap::value);
    }

    @Override
    public ClassInstance getInstance() {
        return instance;
    }
}
