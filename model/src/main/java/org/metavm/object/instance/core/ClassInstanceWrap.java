package org.metavm.object.instance.core;

import java.util.HashMap;
import java.util.Map;

public class ClassInstanceWrap extends InstanceWrap {
    private final Map<String,Object> map;

    public ClassInstanceWrap(Map<String, Object> map) {
        this.map = new HashMap<>(map);
    }

    public Object get(String fieldName) {
        return convertValue(map.get(fieldName));
    }

    public String getString(String fieldName) {
        return (String) get(fieldName);
    }

    public Id getId(String fieldName) {
        return Id.parse(getString(fieldName));
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
}
