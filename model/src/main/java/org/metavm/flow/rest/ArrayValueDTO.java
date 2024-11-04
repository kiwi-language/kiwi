package org.metavm.flow.rest;

import java.util.List;

public record ArrayValueDTO(List<ValueDTO> elements, String type) implements ValueDTO {
    @Override
    public int getKind() {
        return ValueKindCodes.ARRAY;
    }
}
