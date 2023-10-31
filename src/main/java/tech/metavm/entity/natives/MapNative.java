package tech.metavm.entity.natives;

import tech.metavm.entity.CollectionKind;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

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
        ClassType type = instance.getType();
        keyArrayField = type.getFieldByCode("keyArray");
        valueArrayField = type.getFieldByCode("valueArray");
        keyType = ((ArrayType) keyArrayField.getType()).getElementType();
        valueType = ((ArrayType) valueArrayField.getType()).getElementType();
        if (instance.isFieldInitialized(keyArrayField)) {
            keyArray = (ArrayInstance) instance.getField(keyArrayField);
            valueArray = (ArrayInstance) instance.getField(valueArrayField);
            if (valueArray == null || keyArray.length() != valueArray.length()) {
                throw new InternalException("Map data corrupted");
            }
            NncUtils.biForEachWithIndex(keyArray, valueArray, (key, value, index) ->
                    map.put(key, new Entry(value, index))
            );
        }
    }

    public Instance Map() {
        instance.initialize(
                Map.of(
                        keyArrayField,
                        keyArray = new ArrayInstance(
                                (ArrayType) keyArrayField.getType()
                        ),
                        valueArrayField,
                        valueArray = new ArrayInstance(
                                (ArrayType) valueArrayField.getType()
                        )
                ),
                0L,
                0L
        );
        return instance;
    }

    public Instance keySet() {
        var keySetType = (ClassType) instance.getType().getDependency(CollectionKind.SET);
        ClassInstance keySet = ClassInstance.allocate(keySetType);
        var setNative = (SetNative) NativeInvoker.getNativeObject(keySet);
        setNative.Set();
        for (Instance key : keyArray) {
            setNative.add(key);
        }
        return keySet;
    }

    public Instance get(Instance key) {
        checkKey(key);
        var entry = map.get(key);
        return entry != null ? entry.value : InstanceUtils.nullInstance();
    }

    public Instance getOrDefault(Instance key, Instance value) {
        checkKey(key);
        checkValue(value);
        var entry = map.get(key);
        return entry != null ? entry.getValue() : value;
    }

    public Instance put(Instance key, Instance value) {
        checkKey(key);
        checkValue(value);
        var existingEntry = map.get(key);
        if (existingEntry != null) {
            var removed = existingEntry.value;
            int index = existingEntry.index;
            map.put(key, new Entry(value, index));
            keyArray.set(index, key);
            valueArray.set(index, value);
            return removed;
        } else {
            map.put(key, new Entry(value, map.size()));
            keyArray.add(key);
            valueArray.add(value);
            return InstanceUtils.nullInstance();
        }
    }

    public BooleanInstance containsKey(Instance key) {
        checkKey(key);
        return InstanceUtils.booleanInstance(map.containsKey(key));
    }

    public Instance remove(Instance key) {
        checkKey(key);
        var entry = map.remove(key);
        if (entry != null) {
            int lastIndex = keyArray.size() - 1;
            Instance lastKey = keyArray.get(lastIndex);
            Instance lastValue = valueArray.get(lastIndex);
            keyArray.remove(lastIndex);
            valueArray.remove(lastIndex);
            if (!key.equals(lastKey)) {
                keyArray.set(entry.index, lastKey);
                valueArray.set(entry.index, lastValue);
                map.get(lastKey).index = entry.index;
            }
            return entry.getValue();
        } else {
            return InstanceUtils.nullInstance();
        }
    }

    public Instance size() {
        return InstanceUtils.longInstance(keyArray.size());
    }

    public void clear() {
        map.clear();
        keyArray.clear();
        valueArray.clear();
    }

    private boolean getBoolean(Instance instance) {
        if (instance instanceof BooleanInstance booleanInstance) {
            return booleanInstance.isTrue();
        } else {
            throw new InternalException("Expecting a boolean argument, actually got: " + instance);
        }
    }

    private void checkKey(Instance key) {
        NncUtils.requireTrue(keyType.isInstance(key));
    }

    private void checkValue(Instance value) {
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
