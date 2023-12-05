package tech.metavm.entity;

import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexDef<T> {

    public static <T> IndexDef<T> normalKey(Class<T> klass, String...fieldNames) {
        return new IndexDef<>(klass, false, fieldNames);
    }

    public static <T> IndexDef<T> uniqueKey(Class<T> klass, String...fieldNames) {
        return new IndexDef<>(klass, true, fieldNames);
    }

    private final Class<T> type;
    private final Type genericType;
    private final List<String> fieldNames;
    private final boolean unique;

    public IndexDef(TypeReference<T> typeReference, boolean unique, String...fieldNames) {
        this(typeReference.getType(), typeReference.getGenericType(), unique, fieldNames);
    }

    public IndexDef(TypeReference<T> typeReference, String...fieldNames) {
        this(typeReference.getType(), typeReference.getGenericType(), true, fieldNames);
    }

    public IndexDef(Class<T> type, String...fieldNames) {
        this(type, type, true, fieldNames);
    }


    public IndexDef(Class<T> type, boolean unique, String...fieldNames) {
        this(type, type, unique, fieldNames);
    }

    private IndexDef(Class<T> type, Type genericType, boolean unique, String...fieldNames) {
        this.type = type;
        this.genericType = genericType;
        this.unique = unique;
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

    public String getFieldName(int index) {
        return fieldNames.get(index);
    }

    public boolean isUnique() {
        return unique;
    }

    public EntityIndexQueryBuilder<T> newQueryBuilder() {
        return EntityIndexQueryBuilder.newBuilder(this);
    }

}
