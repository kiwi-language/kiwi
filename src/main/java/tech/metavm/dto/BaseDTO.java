package tech.metavm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import tech.metavm.util.TmpIdDeserializer;

public interface BaseDTO {

    Long id();

    @JsonDeserialize(using = TmpIdDeserializer.class)
    Long tmpId();

    @JsonIgnore
    default RefDTO getRef() {
        return new RefDTO(id(), tmpId());
    }

}
