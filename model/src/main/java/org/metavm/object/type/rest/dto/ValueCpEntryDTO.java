package org.metavm.object.type.rest.dto;

import org.metavm.object.instance.rest.FieldValue;

public record ValueCpEntryDTO(int index, FieldValue value) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 0;
    }
}
