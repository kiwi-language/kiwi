package tech.metavm.entity.natives;

import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.Instances;
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
        keyArrayField = type.findFieldByCode("keyArray");
        valueArrayField = type.findFieldByCode("valueArray");
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
        keyArray = new ArrayInstance(
                (ArrayType) keyArrayField.getType(), new InstanceParentRef(instance, keyArrayField)
        );
        valueArray = new ArrayInstance(
                (ArrayType) valueArrayField.getType(), new InstanceParentRef(instance, valueArrayField)
        );
        return instance;
    }

    public ClassInstance keySet() {
        var keySetType = (ClassType) instance.getType().getDependency(StandardTypes.getSetType());
        ClassInstance keySet = ClassInstance.allocate(keySetType);
        var setNative = (SetNative) NativeMethods.getNativeObject(keySet);
        setNative.Set();
        for (Instance key : keyArray) {
            setNative.add(key);
        }
        return keySet;
    }

    public Instance get(Instance key) {
        checkKey(key);
        var entry = map.get(key);
        return entry != null ? entry.value : Instances.nullInstance();
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

    public BooleanInstance containsKey(Instance key) {
        checkKey(key);
        return Instances.booleanInstance(map.containsKey(key));
    }

    public Instance remove(Instance key) {
        checkKey(key);
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

    public Instance size() {
        return Instances.longInstance(keyArray.size());
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
