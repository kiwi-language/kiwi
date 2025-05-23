package org.metavm.object.instance.rest.dto;

import java.util.List;

public record ArrayDTO(List<ValueDTO> elements) implements ValueDTO {
    @Override
    public String getKind() {
        return "array";
    }
}
