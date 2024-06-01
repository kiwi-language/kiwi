package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.rest.dto.FieldRefDTO;

import javax.annotation.Nullable;
import java.util.Objects;

public record UpdateFieldDTO(
        @Nullable FieldRefDTO fieldRef,
        @Nullable String fieldName,
        int opCode,
        ValueDTO value
) {

    @JsonIgnore
    public Field getField(Klass type) {
        if(fieldRef != null) {
            var rawFieldId = Id.parse(fieldRef.rawFieldId());
            return type.getField(f -> f.getEffectiveTemplate().idEquals(rawFieldId));
        }
        else
            return type.getFieldByName(Objects.requireNonNull(fieldName));
    }

    @JsonIgnore
    public Field getStaticField(Klass type) {
        if(fieldRef != null) {
            var rawFiedId = Id.parse(fieldRef.rawFieldId());
            var found = type.findStaticField(f -> f.getEffectiveTemplate().idEquals(rawFiedId));
            if(found != null)
                return found;
            throw new NullPointerException("Can not find static field in klass " + type.getTypeDesc() + " with raw field id: " + rawFiedId);
        }
        else
            return type.getStaticFieldByName(Objects.requireNonNull(fieldName));
    }


}
