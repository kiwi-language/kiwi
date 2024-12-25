package org.metavm.entity.natives;

import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class MapNative extends NativeBase {

    public static final Logger logger = LoggerFactory.getLogger(MapNative.class);

    private final Map<HashKeyWrap, Entry> map = new HashMap<>();
    private final ClassInstance instance;
    private final Type keyType;
    private final Type valueType;
    private final FieldRef keyArrayField;
    private final FieldRef valueArrayField;
    private ArrayInstance keyArray;
    private ArrayInstance valueArray;

    public MapNative(ClassInstance instance) {
        this.instance = instance;
        var type = instance.getType();
        keyArrayField = Objects.requireNonNull(type.findFieldByName("keyArray"));
        valueArrayField = Objects.requireNonNull(type.findFieldByName("valueArray"));
        keyType = ((ArrayType) keyArrayField.getPropertyType()).getElementType();
        valueType = ((ArrayType) valueArrayField.getPropertyType()).getElementType();
        if (instance.isFieldInitialized(keyArrayField.getRawField())) {
            keyArray = instance.getField(keyArrayField.getRawField()).resolveArray();
            valueArray = instance.getField(valueArrayField.getRawField()).resolveArray();
            if (valueArray == null || keyArray.length() != valueArray.length()) {
                throw new InternalException("Map data corrupted");
            }
            var instCtx = Objects.requireNonNull(instance.getContext(), () -> "Context is missing from instance " + instance);
            NncUtils.biForEachWithIndex(keyArray, valueArray, (key, value, index) -> {
                map.put(new HashKeyWrap(key, instCtx), new Entry(value, index));
            });
        }
    }

    public Value HashMap(CallContext callContext) {
        keyArray = new ArrayInstance((ArrayType) keyArrayField.getPropertyType());
        valueArray = new ArrayInstance((ArrayType) valueArrayField.getPropertyType());
        instance.initField(keyArrayField.getRawField(), keyArray.getReference());
        instance.initField(valueArrayField.getRawField(), valueArray.getReference());
        return instance.getReference();
    }

    public Value keySet(CallContext callContext) {
        var keySetKlass = KlassType.create(StdKlass.hashSet.get(), List.of(instance.getType().getFirstTypeArgument()));
        ClassInstance keySet = ClassInstance.allocate(keySetKlass);
        var setNative = (HashSetNative) NativeMethods.getNativeObject(keySet);
        setNative.HashSet(callContext);
        for (Value key : keyArray) {
            setNative.add(key, callContext);
        }
        return keySet.getReference();
    }

    public Value get(Value key, CallContext callContext) {
        checkKey(key, callContext);
        var entry = map.get(new HashKeyWrap(key, callContext));
        return entry != null ? entry.value : Instances.nullInstance();
    }

    public Value getOrDefault(Value key, Value value, CallContext callContext) {
        checkKey(key, callContext);
        checkValue(value, callContext);
        var entry = map.get(new HashKeyWrap(key, callContext));
        return entry != null ? entry.getValue() : value;
    }

    public Value put(Value key, Value value, CallContext callContext) {
        checkKey(key, callContext);
        checkValue(value, callContext);
        var keyWrap = new HashKeyWrap(key, callContext);
        var existingEntry = map.get(keyWrap);
        if (existingEntry != null) {
            var removed = existingEntry.value;
            int index = existingEntry.index;
            map.put(keyWrap, new Entry(value, index));
            keyArray.setElement(index, key);
            valueArray.setElement(index, value);
            return removed;
        } else {
            map.put(keyWrap, new Entry(value, map.size()));
            keyArray.addElement(key);
            valueArray.addElement(value);
            return Instances.nullInstance();
        }
    }

    public Value containsKey(Value key, CallContext callContext) {
        return Instances.intInstance(containsKey0(key, callContext));
    }

    public boolean containsKey0(Value key, CallContext callContext) {
        checkKey(key, callContext);
        return map.containsKey(new HashKeyWrap(key, callContext));
    }

    public Value remove(Value key, CallContext callContext) {
        checkKey(key, callContext);
        var entry = map.remove(new HashKeyWrap(key, callContext));
        if (entry != null) {
            int lastIndex = keyArray.size() - 1;
            Value lastKey = keyArray.get(lastIndex);
            Value lastValue = valueArray.get(lastIndex);
            keyArray.removeElement(lastIndex);
            valueArray.removeElement(lastIndex);
            if (!key.equals(lastKey)) {
                keyArray.setElement(entry.index, lastKey);
                valueArray.setElement(entry.index, lastValue);
                map.get(new HashKeyWrap(lastKey, callContext)).index = entry.index;
            }
            return entry.getValue();
        } else {
            return Instances.nullInstance();
        }
    }

    public Value size(CallContext callContext) {
        return Instances.intInstance(size());
    }

    private int size() {
        return keyArray.size();
    }

    public Value clear(CallContext callContext) {
        map.clear();
        keyArray.clear();
        valueArray.clear();
        return Instances.nullInstance();
    }

    public Value hashCode(CallContext callContext) {
        int h = 0;
        int i = 0;
        for (Value key : keyArray) {
            var value = valueArray.get(i++);
            h = h + (Instances.hashCode(key, callContext) ^ Instances.hashCode(value, callContext));
        }
        return Instances.intInstance(h);
    }

    public Value equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.resolve() instanceof ClassInstance that
                    && Objects.equals(that.getType().findAncestorByKlass(StdKlass.map.get()), instance.getType().findAncestorByKlass(StdKlass.map.get()))) {
                var thatNat = new MapNative(that);
                if(keyArray.size() == thatNat.size()) {
                    int i = 0;
                    for (Value key : keyArray) {
                        if(!thatNat.containsKey0(key, callContext))
                            return Instances.zero();
                        if(!Instances.equals(valueArray.get(i++), thatNat.get(key, callContext), callContext))
                            return Instances.zero();
                    }
                    return Instances.one();
                }
            }
        }
        return Instances.zero();
    }

    private boolean getBoolean(Value instance, CallContext callContext) {
        if (instance instanceof BooleanValue booleanInstance) {
            return booleanInstance.isTrue();
        } else {
            throw new InternalException("Expecting a boolean argument, actually got: " + instance);
        }
    }

    private void checkKey(Value key, CallContext callContext) {
        NncUtils.requireTrue(keyType.isInstance(key));
    }

    private void checkValue(Value value, CallContext callContext) {
        NncUtils.requireTrue(valueType.isInstance(value));
    }

    private static class Entry {
        private final Value value;
        private int index;

        public Entry(Value value, int index) {
            this.value = value;
            this.index = index;
        }

        public Value getValue() {
            return value;
        }
    }

}
