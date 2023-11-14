package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.dto.RefDTO;
import tech.metavm.object.instance.InstanceParamTypeIdResolver;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record InstanceDTO(
        @Nullable Long id,
        RefDTO typeRef,
        String typeName,
        String title,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonTypeIdResolver(InstanceParamTypeIdResolver.class)
        InstanceParamDTO param
) {

    public static InstanceDTO valueOf(@Nullable Long id, long typeId, String title, List<InstanceFieldDTO> fields){
        return new InstanceDTO(id, RefDTO.fromId(typeId), null, title, new ClassInstanceParam(fields));
    }

    public static InstanceDTO valueOf(long typeId, List<InstanceFieldDTO> fields) {
        return valueOf(null, typeId, fields);
    }

    public static InstanceDTO valueOf(@Nullable Long id, long typeId, List<InstanceFieldDTO> fields) {
        return new InstanceDTO(
                id,
                RefDTO.fromId(typeId),
                null,
                null,
                new ClassInstanceParam(fields)
        );
    }

    public static InstanceDTO createArray(Long id, long typeId, List<FieldValue> elements){
        return new InstanceDTO(
                id,
                RefDTO.fromId(typeId),
                null,
                null,
                new ArrayParamDTO(false, elements)
        );
    }

    public InstanceDTO copyWithParam(InstanceParamDTO param) {
        return new InstanceDTO(id, typeRef, typeName, title, param);
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
}
