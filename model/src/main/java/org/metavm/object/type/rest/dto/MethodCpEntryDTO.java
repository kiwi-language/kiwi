package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.MethodRefDTO;

public record MethodCpEntryDTO(int index, MethodRefDTO methodRef) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 3;
    }
}
