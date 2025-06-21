package org.metavm.object.instance.core;

import org.metavm.util.ApiNamedObject;
import org.metavm.util.Utils;

import java.util.*;

public class ApiObject {
    private final Map<String,Object> map;
    private final Id id;
    private final Map<String, Object> fields = new HashMap<>();
    private final Map<String, List<ApiObject>> children = new HashMap<>();

    public ApiObject(Map<String, Object> map) {
        this.map = new HashMap<>(map);
        id = Utils.safeCall((String) map.get("id"), Id::parse);
        //noinspection unchecked
        var fields = (Map<String, Object>) map.getOrDefault("fields", Map.of());
        fields.forEach((name, value) -> this.fields.put(name, convertValue(value)));
        //noinspection unchecked
        var children = (Map<String, List<Object>>) map.getOrDefault("children", Map.of());
        children.forEach((name, c) -> {
            var c1 = new ArrayList<ApiObject>();
            c.forEach(e -> c1.add((ApiObject) convertValue(e)));
            this.children.put(name, c1);
        });
    }

    public static ApiObject from(Object value) {
        var inst = convertValue(value);
        if(inst instanceof ApiObject apiObject)
            return apiObject;
        else
            throw new IllegalArgumentException("Invalid object: " + value);
    }

    public static Object convertValue(Object value) {
        if(value instanceof Map<?,?> m) {
            if (m.get("name") instanceof String name) {
                if (m.get("type") instanceof String type)
                   return new ApiNamedObject(type, name, (String) m.get("summary"));
                else
                    return ApiNamedObject.of(name);
            }
            else if (m.containsKey("fields")) {
                //noinspection unchecked
                return new ApiObject((Map<String, Object>) m);
            }
            else
                return Id.parse((String) Objects.requireNonNull(m.get("id"), () -> "Invalid object: " + m));
        } else if(value instanceof List<?> l) {
            var convertedList = new ArrayList<>();
            l.forEach(e -> convertedList.add(convertValue(e)));
            return convertedList;
        } else
            return value;
    }

    public Object get(String fieldName) {
        return fields.get(fieldName);
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

    public float getFloat(String fieldName) {
        return (float) get(fieldName);
    }

    public String getString(String fieldName) {
        return (String) get(fieldName);
    }

    public ApiNamedObject getEnumConstant(String fieldName) {
        return (ApiNamedObject) get(fieldName);
    }

    public boolean getBoolean(String fieldName) {
        return (boolean) get(fieldName);
    }

    public Id getId(String fieldName) {
        return (Id) get(fieldName);
    }

    public Id id() {
        return id;
    }

    public ApiObject getObject(String fieldName) {
        var value = get(fieldName);
        if(value instanceof ApiObject apiObject)
            return apiObject;
        else
            throw new IllegalStateException("Field '" + fieldName + "' is not an object: " + value);
    }

    public List<?> getArray(String fieldName) {
        var value = get(fieldName);
        if(value instanceof List<?> array)
            return array;
        else
            throw new IllegalStateException("Field '" + fieldName + "' is not an array: " + value);
    }

    public List<ApiObject> getChildren(String className) {
        return children.getOrDefault(className, List.of());
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public Map<String, Object> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ApiObject that && map.equals(that.map);
    }


}
