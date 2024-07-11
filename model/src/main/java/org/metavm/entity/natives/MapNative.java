package org.metavm.entity.natives;

import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class MapNative extends NativeBase {

    private final Map<Instance, Entry> map = new HashMap<>();
    private final ClassInstance instance;
    private final Type keyType;
    private final Type valueType;
    private final Field keyArrayField;
    private final Field valueArrayField;
    private ArrayInstance keyArray;
    private ArrayInstance valueArray;

    public MapNative(ClassInstance instance) {
        this.instance = instance;
        Klass type = instance.getKlass();
        keyArrayField = Objects.requireNonNull(type.findFieldByCode("keyArray"));
        valueArrayField = Objects.requireNonNull(type.findFieldByCode("valueArray"));
        keyType = ((ArrayType) keyArrayField.getType()).getElementType();
        valueType = ((ArrayType) valueArrayField.getType()).getElementType();
        if (instance.isFieldInitialized(keyArrayField)) {
            keyArray = instance.getField(keyArrayField).resolveArray();
            valueArray = instance.getField(valueArrayField).resolveArray();
            if (valueArray == null || keyArray.length() != valueArray.length()) {
                throw new InternalException("Map data corrupted");
            }
            NncUtils.biForEachWithIndex(keyArray, valueArray, (key, value, index) ->
                    map.put(key, new Entry(value, index))
            );
        }
    }

    public Instance HashMap(CallContext callContext) {
        keyArray = new ArrayInstance((ArrayType) keyArrayField.getType());
        valueArray = new ArrayInstance((ArrayType) valueArrayField.getType());
        instance.initField(keyArrayField, keyArray.getReference());
        instance.initField(valueArrayField, valueArray.getReference());
        return instance.getReference();
    }

    public InstanceReference keySet(CallContext callContext) {
        var keySetKlass = StdKlass.hashSet.get().getParameterized(List.of(instance.getKlass().getFirstTypeArgument()));
        ClassInstance keySet = ClassInstance.allocate(keySetKlass.getType());
        var setNative = (SetNative) NativeMethods.getNativeObject(keySet);
        setNative.HashSet(callContext);
        for (Instance key : keyArray) {
            setNative.add(key, callContext);
        }
        return keySet.getReference();
    }

    public Instance get(Instance key, CallContext callContext) {
        checkKey(key, callContext);
        var entry = map.get(key);
        return entry != null ? entry.value : Instances.nullInstance();
    }

    public Instance getOrDefault(Instance key, Instance value, CallContext callContext) {
        checkKey(key, callContext);
        checkValue(value, callContext);
        var entry = map.get(key);
        return entry != null ? entry.getValue() : value;
    }

    public Instance put(Instance key, Instance value, CallContext callContext) {
        checkKey(key, callContext);
        checkValue(value, callContext);
        var existingEntry = map.get(key);
        if (existingEntry != null) {
            var removed = existingEntry.value;
            int index = existingEntry.index;
            map.put(key, new Entry(value, index));
            keyArray.setElement(index, key);
            valueArray.setElement(index, value);
            return removed;
        } else {
            map.put(key, new Entry(value, map.size()));
            keyArray.addElement(key);
            valueArray.addElement(value);
            return Instances.nullInstance();
        }
    }

    public BooleanInstance containsKey(Instance key, CallContext callContext) {
        checkKey(key, callContext);
        return Instances.booleanInstance(map.containsKey(key));
    }

    public Instance remove(Instance key, CallContext callContext) {
        checkKey(key, callContext);
        var entry = map.remove(key);
        if (entry != null) {
            int lastIndex = keyArray.size() - 1;
            Instance lastKey = keyArray.get(lastIndex);
            Instance lastValue = valueArray.get(lastIndex);
            keyArray.removeElement(lastIndex);
            valueArray.removeElement(lastIndex);
            if (!key.equals(lastKey)) {
                keyArray.setElement(entry.index, lastKey);
                valueArray.setElement(entry.index, lastValue);
                map.get(lastKey).index = entry.index;
            }
            return entry.getValue();
        } else {
            return Instances.nullInstance();
        }
    }

    public Instance size(CallContext callContext) {
        return Instances.longInstance(keyArray.size());
    }

    public void clear(CallContext callContext) {
        map.clear();
        keyArray.clear();
        valueArray.clear();
    }

    private boolean getBoolean(Instance instance, CallContext callContext) {
        if (instance instanceof BooleanInstance booleanInstance) {
            return booleanInstance.isTrue();
        } else {
            throw new InternalException("Expecting a boolean argument, actually got: " + instance);
        }
    }

    private void checkKey(Instance key, CallContext callContext) {
        NncUtils.requireTrue(keyType.isInstance(key));
    }

    private void checkValue(Instance value, CallContext callContext) {
        NncUtils.requireTrue(valueType.isInstance(value));
    }

    private static class Entry {
        private final Instance value;
        private int index;

        public Entry(Instance value, int index) {
            this.value = value;
            this.index = index;
        }

        public Instance getValue() {
            return value;
        }
    }

}
