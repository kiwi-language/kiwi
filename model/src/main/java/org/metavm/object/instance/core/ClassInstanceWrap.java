package org.metavm.object.instance.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassInstanceWrap extends InstanceWrap {
    private final Map<String,Object> map;

    public ClassInstanceWrap(Map<String, Object> map) {
        this.map = new HashMap<>(map);
    }

    public Object getRaw(String fieldName) {
        return map.get(fieldName);
    }

    public Object get(String fieldName) {
        return convertValue(getRaw(fieldName));
    }

    public long getLong(String fieldName) {
        return (long) get(fieldName);
    }

    public double getDouble(String fieldName) {
        return (double) get(fieldName);
    }

    public String getString(String fieldName) {
        return (String) get(fieldName);
    }

    public boolean getBoolean(String fieldName) {
        return (boolean) get(fieldName);
    }

    public Id getId(String fieldName) {
        return Id.parse(getString(fieldName));
    }

    public String id() {
        return (String) map.get("$id");
    }

    public ClassInstanceWrap getObject(String fieldName) {
        var value = get(fieldName);
        if(value instanceof ClassInstanceWrap classInstanceWrap)
            return classInstanceWrap;
        else
            throw new IllegalStateException("Field '" + fieldName + "' is not an object: " + value);
    }

    public ArrayInstanceWrap getArray(String fieldName) {
        var value = get(fieldName);
        if(value instanceof ArrayInstanceWrap array)
            return array;
        else
            throw new IllegalStateException("Field '" + fieldName + "' is not an array: " + value);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
