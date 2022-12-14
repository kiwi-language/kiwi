package tech.metavm.entity;

import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexDef<T extends Entity> {

    private final Class<T> type;
    private final Type genericType;
    private final List<String> fieldNames;

    public IndexDef(TypeReference<T> typeReference, String...fieldNames) {
        this(typeReference.getType(), typeReference.getGenericType(), fieldNames);
    }

    public IndexDef(Class<T> type, String...fieldNames) {
        this(type, type, fieldNames);
    }

    private IndexDef(Class<T> type, Type genericType, String...fieldNames) {
        this.type = type;
        this.genericType = genericType;
        this.fieldNames = new ArrayList<>(Arrays.asList(fieldNames));
    }

    public Class<T> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

}
