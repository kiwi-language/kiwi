package org.metavm.object.instance.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ApiObject(Map<String, Object> map) {
    public ApiObject(Map<String, Object> map) {
        this.map = new HashMap<>(map);
    }

    public static ApiObject from(Map<String, Object> value) {
        return new ApiObject(value);
    }

    public Object get(String fieldName) {
        return map.get(fieldName);
    }

    public long getLong(String fieldName) {
        return (long) get(fieldName);
    }

    public int getInt(String fieldName) {
        return (int) get(fieldName);
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

    public String id() {
        return (String) map.get("$id");
    }

    public ApiObject getObject(String fieldName) {
        //noinspection unchecked
        return from((Map<String, Object>) get(fieldName));
    }

    public List<?> getArray(String fieldName) {
        var value = get(fieldName);
        if (value instanceof List<?> array)
            return array;
        else
            throw new IllegalStateException("Field '" + fieldName + "' is not an array: " + value);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public Map<String, Object> map() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ApiObject that && map.equals(that.map);
    }


}
