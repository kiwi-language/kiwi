package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.KlassType;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class HashSetNative extends AbstractSetNative {

    private final ClassInstance instance;
    private final Set<HashKeyWrap> set = new HashSet<>();

    public HashSetNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value HashSet(CallContext callContext) {
        return instance.getReference();
    }

    public Value HashSet__Collection(Value c, CallContext callContext) {
        var collNative = (CollectionNative) NativeMethods.getNativeObject(c.resolveObject());
        collNative.forEach(e -> set.add(new HashKeyWrap(e, callContext)));
        return instance.getReference();
    }

    @Override
    public Value iterator(CallContext callContext) {
        var iteratorImplType = KlassType.create(StdKlass.iteratorImpl.get(), List.of(instance.getInstanceType().getFirstTypeArgument()));
        var it = ClassInstance.allocate(TmpId.random(), iteratorImplType);
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
        return Utils.mapIterator(set.iterator(), HashKeyWrap::value);
    }

    @Override
    public Value add(Value value, CallContext callContext) {
        var keyWrap = new HashKeyWrap(value, callContext);
        return Instances.intInstance(set.add(keyWrap));
    }

    @Override
    public Value remove(Value value, CallContext callContext) {
        var keyWrap = new HashKeyWrap(value, callContext);
        return Instances.intInstance(set.remove(keyWrap));
    }

    @Override
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

    @Override
    public Value clear(CallContext callContext) {
        set.clear();
        return Instances.nullInstance();
    }

    @Override
    public Value forEach(Value action, CallContext callContext) {
        if(action instanceof FunctionValue functionValue) {
            set.forEach(e -> functionValue.execute(List.of(e.value()), callContext));
            return Instances.nullInstance();
        } else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    public Value writeObject(Value s, CallContext callContext) {
        var out = Instances.extractOutput(s);
        out.writeInt(set.size());
        for (var e : set) {
            out.writeValue(e.value());
        }
        return Instances.nullInstance();
    }

    public Value readObject(Value s, CallContext callContext) {
        var in = Instances.extractInput(s);
        var size = in.readInt();
        for (int i = 0; i < size; i++) {
            set.add(new HashKeyWrap(in.readValue(), callContext));
        }
        return Instances.nullInstance();
    }

}
