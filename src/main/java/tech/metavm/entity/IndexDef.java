package tech.metavm.entity;

import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexDef<T extends Entity> {

    private final Class<T> entityType;
    private final List<String> fieldNames;

    public IndexDef(TypeReference<T> typeReference, String...fieldNames) {
        this(typeReference.getType(), fieldNames);
    }

    public IndexDef(Class<T> entityType, String...fieldNames) {
        this.entityType = entityType;
        this.fieldNames = new ArrayList<>(Arrays.asList(fieldNames));
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }
}
