package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.metavm.object.instance.InstanceParamTypeIdResolver;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.view.rest.dto.ObjectMappingRefDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record InstanceDTO(
        @Nullable String id,
        String type,
        String typeName,
        String title,
        @Nullable ObjectMappingRefDTO sourceMappingRef,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonTypeIdResolver(InstanceParamTypeIdResolver.class)
        InstanceParam param
) implements Serializable {

    public static InstanceDTO createClassInstance(String type, List<InstanceFieldDTO> fields) {
        return createClassInstance(null, type, fields);
    }

    public static InstanceDTO createClassInstance(@Nullable String id, String type, List<InstanceFieldDTO> fields) {
        return createClassInstance(id, type, null, fields);
    }

    public static InstanceDTO createClassInstance(@Nullable String id, String type, ObjectMappingRefDTO sourceMappingRef, List<InstanceFieldDTO> fields) {
        return new InstanceDTO(
                id,
                type,
                null,
                null,
                sourceMappingRef,
                new ClassInstanceParam(fields)
        );
    }

    public static InstanceDTO createArrayInstance(String type, boolean elementAsChild, List<FieldValue> elements) {
        return createArrayInstance(null, type, elementAsChild, elements);
    }

    public static InstanceDTO createArrayInstance(@Nullable String id, String type, boolean elementAsChild, List<FieldValue> elements) {
        return new InstanceDTO(
                id, type, null, null, null,
                new ArrayInstanceParam(elementAsChild, elements)
        );
    }

    public static InstanceDTO createListInstance(String type, boolean elementAsChild, List<FieldValue> elements) {
        return createListInstance(null, type, elementAsChild, elements);
    }

    public static InstanceDTO createListInstance(@Nullable String id, String type, boolean elementAsChild, List<FieldValue> elements) {
        return new InstanceDTO(
                id, type, null, null, null,
                new ListInstanceParam(elementAsChild, elements)
        );
    }


    public InstanceDTO copyWithParam(InstanceParam param) {
        return new InstanceDTO(id, type, typeName, title, sourceMappingRef, param);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceDTO that = (InstanceDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(typeName, that.typeName) && Objects.equals(title, that.title) && Objects.equals(param, that.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, typeName, title, param);
    }


    public Id parseId() {
        return Id.parse(Objects.requireNonNull(id));
    }

    @JsonIgnore
    public FieldValue getFieldValue(String fieldId) {
        var param = (ClassInstanceParam) param();
        return NncUtils.findRequired(param.fields(), f -> Objects.equals(f.fieldId(), fieldId)).value();
    }

    @JsonIgnore
    public FieldValue getFieldValueByName(String fieldName) {
        var param = (ClassInstanceParam) param();
        return NncUtils.findRequired(param.fields(), f -> Objects.equals(f.fieldName(), fieldName)).value();
    }

    @JsonIgnore
    public String getReferenceId(String fieldName) {
        return ((ReferenceFieldValue) getFieldValueByName(fieldName)).getId();
    }

    @JsonIgnore
    public Object getPrimitiveValue(String fieldName) {
        return ((PrimitiveFieldValue) getFieldValueByName(fieldName)).getValue();
    }

    @JsonIgnore
    public InstanceDTO getInstance(String fieldName) {
        return ((InstanceFieldValue) getFieldValueByName(fieldName)).getInstance();
    }

    @JsonIgnore
    public FieldValue getElement(int index) {
        if (param() instanceof ArrayInstanceParam arrayInstanceParam)
            return arrayInstanceParam.elements().get(index);
        else if (param() instanceof ListInstanceParam listInstanceParam)
            return listInstanceParam.elements().get(index);
        else
            throw new IllegalStateException("Not an array or list instance");
    }

    @JsonIgnore
    public String getIdRequired() {
        return Objects.requireNonNull(id);
    }

    @JsonIgnore
    public InstanceDTO getElementInstance(int index) {
        return ((InstanceFieldValue) getElement(index)).getInstance();
    }

    @JsonIgnore
    public List<FieldValue> getElements() {
        if (param() instanceof ArrayInstanceParam arrayInstanceParam)
            return arrayInstanceParam.elements();
        else if (param() instanceof ListInstanceParam listInstanceParam)
            return listInstanceParam.elements();
        else
            throw new IllegalStateException("Not an array or list instance");
    }

    @JsonIgnore
    public int getArraySize() {
        var param = (ArrayInstanceParam) param();
        return param.elements().size();
    }

    @JsonIgnore
    public int getListSize() {
        var param = (ListInstanceParam) param();
        return param.elements().size();
    }

    @JsonIgnore
    public int arraySize() {
        var param = (ArrayInstanceParam) param();
        return param.elements().size();
    }

    public boolean valueEquals(InstanceDTO that, Set<String> newIds) {
        return (Objects.equals(id, that.id) || newIds.contains(id) && that.id == null || newIds.contains(that.id) && id == null)
                && Objects.equals(type, that.type)
                && param.valueEquals(that.param, newIds);
    }

    @JsonIgnore
    public boolean isNew() {
        return id == null || Id.parse(id) instanceof TmpId;
    }

    public Object toJson() {
        return switch (param) {
            case ClassInstanceParam classInstanceParam -> {
                var map = new HashMap<String, Object>();
                if (id != null)
                    map.put("$id", id);
                for (InstanceFieldDTO field : classInstanceParam.fields()) {
                    map.put(field.fieldName(), field.value().toJson());
                }
                yield  map;
            }
            case ArrayInstanceParam arrayInstanceParam ->
                    NncUtils.map(arrayInstanceParam.elements(), FieldValue::toJson);
            case ListInstanceParam listInstanceParam -> NncUtils.map(listInstanceParam.elements(), FieldValue::toJson);
            case PrimitiveInstanceParam primitiveInstanceParam -> primitiveInstanceParam.value();
            default -> throw new IllegalStateException("Unrecognized instance param: " + param);
        };
    }

}
