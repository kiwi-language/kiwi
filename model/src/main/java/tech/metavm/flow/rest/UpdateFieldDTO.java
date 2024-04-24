package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;

import javax.annotation.Nullable;
import java.util.Objects;

public record UpdateFieldDTO(
        @Nullable String fieldId,
        @Nullable String fieldName,
        int opCode,
        ValueDTO value
) {

    @JsonIgnore
    public Field getField(Klass type) {
        if(fieldId != null)
            return type.getField(Id.parse(fieldId));
        else
            return type.getFieldByName(Objects.requireNonNull(fieldName));
    }

    @JsonIgnore
    public Field getStaticField(Klass type) {
        if(fieldId != null)
            return type.getStaticField(Id.parse(fieldId));
        else
            return type.getStaticFieldByName(Objects.requireNonNull(fieldName));
    }


}
