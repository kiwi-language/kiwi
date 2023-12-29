package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.common.RefDTO;
import tech.metavm.object.instance.InstanceParamTypeIdResolver;
import tech.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public record InstanceDTO(
        @Nullable String id,
        RefDTO typeRef,
        String typeName,
        String title,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonTypeIdResolver(InstanceParamTypeIdResolver.class)
        InstanceParam param
) implements Serializable  {


    public InstanceDTO copyWithParam(InstanceParam param) {
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


    public Id parseId() {
        return Id.parse(Objects.requireNonNull(id));
    }

}
