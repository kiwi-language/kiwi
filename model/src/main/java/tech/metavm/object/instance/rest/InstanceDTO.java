package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.InstanceParamTypeIdResolver;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record InstanceDTO(
        @Nullable String id,
        RefDTO typeRef,
        String typeName,
        String title,
        @Nullable Long sourceMappingId,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonTypeIdResolver(InstanceParamTypeIdResolver.class)
        InstanceParam param
) implements Serializable {

    public static InstanceDTO createClassInstance(RefDTO typeRef, List<InstanceFieldDTO> fields) {
        return createClassInstance(null, typeRef, fields);
    }

    public static InstanceDTO createClassInstance(@Nullable String id, RefDTO typeRef, List<InstanceFieldDTO> fields) {
        return new InstanceDTO(
                id,
                typeRef,
                null,
                null,
                null,
                new ClassInstanceParam(fields)
        );
    }

    public static InstanceDTO createArrayInstance(RefDTO typeRef, boolean elementAsChild, List<FieldValue> elements) {
        return createArrayInstance(null, typeRef, elementAsChild, elements);
    }

    public static InstanceDTO createArrayInstance(@Nullable String id, RefDTO typeRef, boolean elementAsChild, List<FieldValue> elements) {
        return new InstanceDTO(
                id, typeRef, null, null, null,
                new ArrayInstanceParam(elementAsChild, elements)
        );
    }

    public static InstanceDTO createListInstance(RefDTO typeRef, boolean elementAsChild, List<FieldValue> elements) {
        return createListInstance(null, typeRef, elementAsChild, elements);
    }

    public static InstanceDTO createListInstance(@Nullable String id, RefDTO typeRef, boolean elementAsChild, List<FieldValue> elements) {
        return new InstanceDTO(
                id, typeRef, null, null, null,
                new ListInstanceParam(elementAsChild, elements)
        );
    }


    public InstanceDTO copyWithParam(InstanceParam param) {
        return new InstanceDTO(id, typeRef, typeName, title, sourceMappingId, param);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceDTO that = (InstanceDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(typeRef, that.typeRef) && Objects.equals(typeName, that.typeName) && Objects.equals(title, that.title) && Objects.equals(param, that.param);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeRef, typeName, title, param);
    }


    public Id parseId() {
        return Id.parse(Objects.requireNonNull(id));
    }

    @JsonIgnore
    public FieldValue getFieldValue(long fieldId) {
        var param = (ClassInstanceParam) param();
        return NncUtils.findRequired(param.fields(), f -> f.fieldId() == fieldId).value();
    }

    @JsonIgnore
    public FieldValue getElement(int index) {
        if(param() instanceof ArrayInstanceParam arrayInstanceParam)
            return arrayInstanceParam.elements().get(index);
        else if(param() instanceof ListInstanceParam listInstanceParam)
            return listInstanceParam.elements().get(index);
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
                && Objects.equals(typeRef, that.typeRef)
                && param.valueEquals(that.param, newIds);
    }

}
