package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.FunctionRefDTO;

public record FunctionCpEntryDTO(int index, FunctionRefDTO functionRef) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 4;
    }
}
