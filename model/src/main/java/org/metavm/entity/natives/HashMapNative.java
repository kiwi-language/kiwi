package org.metavm.entity.natives;

import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Type;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class HashMapNative extends AbstractMapNative implements StatefulNative {

    public static final Logger logger = LoggerFactory.getLogger(HashMapNative.class);

    private Map<HashKeyWrap, Value> map = new HashMap<>();
    private final ClassInstance instance;
    private final Type keyType;
    private final Type valueType;

    public HashMapNative(ClassInstance instance) {
        this.instance = instance;
        keyType = instance.getInstanceType().getTypeArguments().getFirst();
        valueType = instance.getInstanceType().getTypeArguments().getLast();
        var type = instance.getInstanceType();
    }

    public Value HashMap(CallContext callContext) {
        return instance.getReference();
    }

    @Override
    public Value keySet(CallContext callContext) {
        var keySetKlass = KlassType.create(StdKlass.hashSet.get(), List.of(instance.getInstanceType().getFirstTypeArgument()));
        var keySet = ClassInstance.allocate(TmpId.random(), keySetKlass);
        var setNative = (HashSetNative) NativeMethods.getNativeObject(keySet);
        setNative.HashSet(callContext);
        for (var key : map.keySet()) {
            setNative.add(key.value(), callContext);
        }
        return keySet.getReference();
    }

    @Override
    public Value get(Value key, CallContext callContext) {
        checkKey(key, callContext);
        var v = map.get(new HashKeyWrap(key, callContext));
        return v != null ? v : Instances.nullInstance();
    }

    @Override
    public Value getOrDefault(Value key, Value value, CallContext callContext) {
        checkKey(key, callContext);
        checkValue(value, callContext);
        var v = map.get(new HashKeyWrap(key, callContext));
        return v != null ? v : Instances.nullInstance();
    }

    @Override
    public Value put(Value key, Value value, CallContext callContext) {
        checkKey(key, callContext);
        checkValue(value, callContext);
        var old = map.put(new HashKeyWrap(key, callContext), value);
        return old != null ? old : Instances.nullInstance();
    }

    @Override
    public Value containsKey(Value key, CallContext callContext) {
        return Instances.intInstance(containsKey0(key, callContext));
    }

    public boolean containsKey0(Value key, CallContext callContext) {
        checkKey(key, callContext);
        return map.containsKey(new HashKeyWrap(key, callContext));
    }

    @Override
    public Value remove(Value key, CallContext callContext) {
        checkKey(key, callContext);
        var v = map.remove(new HashKeyWrap(key, callContext));
        return v != null ? v : Instances.nullInstance();
    }

    @Override
    public Value size(CallContext callContext) {
        return Instances.intInstance(size());
    }

    private int size() {
        return map.size();
    }

    @Override
    public Value clear(CallContext callContext) {
        map.clear();
        return Instances.nullInstance();
    }

    @Override
    public Value hashCode(CallContext callContext) {
        int h = 0;
        int i = 0;
        for (var e : map.entrySet()) {
            var key = e.getKey().value();
            var value = e.getValue();
            h = h + (Instances.hashCode(key, callContext) ^ Instances.hashCode(value, callContext));
        }
        return Instances.intInstance(h);
    }

    @Override
    public Value equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.get() instanceof MvClassInstance that
                    && Objects.equals(that.getInstanceType().asSuper(StdKlass.map.get()), instance.getInstanceType().asSuper(StdKlass.map.get()))) {
                var thatNat = (HashMapNative) Objects.requireNonNull(that.getNativeObject());
                if(map.size() == thatNat.size()) {
                    int i = 0;
                    for (var e : map.entrySet()) {
                        if(!thatNat.containsKey0(e.getKey().value(), callContext))
                            return Instances.zero();
                        if(!Instances.equals(e.getValue(), thatNat.get(e.getKey().value(), callContext), callContext))
                            return Instances.zero();
                    }
                    return Instances.one();
                }
            }
        }
        return Instances.zero();
    }

    public Value writeObject(Value o, CallContext callContext) {
        var out = ((MvObjectOutputStream) o.resolveObject()).getOut();
        out.writeInt(map.size());
        map.forEach((kw, v) -> {
            out.writeValue(kw.value());
            out.writeValue(v);
        });
        return Instances.nullInstance();
    }

    public Value readObject(Value o, CallContext callContext) {
        var in = ((MvObjectInputStream) o.resolveObject()).getInput();
        var size = in.readInt();
        for (int i = 0; i < size; i++) {
            map.put(
                    new HashKeyWrap(in.readValue(), callContext),
                    in.readValue()
            );
        }
        return Instances.nullInstance();
    }

    private boolean getBoolean(Value instance, CallContext callContext) {
        if (instance instanceof BooleanValue booleanInstance) {
            return booleanInstance.isTrue();
        } else {
            throw new InternalException("Expecting a boolean argument, actually got: " + instance);
        }
    }

    private void checkKey(Value key, CallContext callContext) {
        Utils.require(keyType.isInstance(key));
    }

    private void checkValue(Value value, CallContext callContext) {
        Utils.require(valueType.isInstance(value));
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        map.forEach((kw, v) -> {
            if (kw.value() instanceof Reference r) action.accept(r);
            if (v instanceof Reference r) action.accept(r);
        });
    }

    @Override
    public void forEachReference(BiConsumer<Reference, Boolean> action) {
        map.forEach((kw, v) -> {
            if (kw.value() instanceof Reference r) action.accept(r, false);
            if (v instanceof Reference r) action.accept(r, false);
        });
    }

    @Override
    public void forEachReference(TriConsumer<Reference, Boolean, Type> action) {
        map.forEach((kw, v) -> {
            if (kw.value() instanceof Reference r) action.accept(r, false, keyType);
            if (v instanceof Reference r) action.accept(r, false, valueType);
        });
    }

    @Override
    public void transformReference(TriFunction<Reference, Boolean, Type, Reference> function) {
        var newMap = new HashMap<HashKeyWrap, Value>();
        map.forEach((kw, v) -> {
            if (kw.value() instanceof Reference r)
                kw = new HashKeyWrap(function.apply(r, false, keyType), kw.callContext());
            if (v instanceof Reference r)
                v = function.apply(r, false ,valueType);
            newMap.put(kw, v);
        });
        map = newMap;
    }

}
