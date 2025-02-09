package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.KlassType;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.function.Consumer;

@Slf4j
public class TreeSetNative extends AbstractSetNative implements NavigableSetNative {

    private final ClassInstance instance;
    private final TreeSet<ComparableKeyWrap> set = new TreeSet<>();

    public TreeSetNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value TreeSet(CallContext callContext) {
        return instance.getReference();
    }

    public Value TreeSet(Value c, CallContext callContext) {
        if(c instanceof Reference collection) {
            var collNative = (CollectionNative) NativeMethods.getNativeObject(collection.resolveObject());
            collNative.forEach(e -> set.add(new ComparableKeyWrap(e, callContext)));
            return instance.getReference();
        }
        else
            throw new BusinessException(ErrorCode.ILLEGAL_ARGUMENT);
    }

    @Override
    public Value iterator(CallContext callContext) {
        var iteratorImplType = KlassType.create(StdKlass.iteratorImpl.get(), List.of(instance.getInstanceType().getFirstTypeArgument()));
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
    public Value first(CallContext callContext) {
        if(set.isEmpty()) {
            throw new NoSuchElementException();
        }
        return set.first().value();
    }

    @Override
    public Value add(Value value, CallContext callContext) {
        var keyWrap = new ComparableKeyWrap(value, callContext);
        return Instances.intInstance(set.add(keyWrap));
    }

    @Override
    public Value remove(Value value, CallContext callContext) {
        var keyWrap = new ComparableKeyWrap(value, callContext);
        return Instances.intInstance(set.remove(keyWrap));
    }

    @Override
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

    public @NotNull Iterator<Value> iterator() {
        return Utils.mapIterator(set.iterator(), ComparableKeyWrap::value);
    }

    @Override
    public ClassInstance getInstance() {
        return instance;
    }

    @Override
    public Value getFirst(CallContext callContext) {
        var first = set.getFirst();
        return first != null ? first.value() : Instances.nullInstance();
    }

    @Override
    public Value getLast(CallContext callContext) {
        var last = set.getLast();
        return last != null ? last.value() : Instances.nullInstance();
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
            set.add(new ComparableKeyWrap(in.readValue(), callContext));
        }
        return Instances.nullInstance();
    }

}
