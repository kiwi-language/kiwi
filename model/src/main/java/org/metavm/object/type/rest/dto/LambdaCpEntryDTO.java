package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.LambdaRefDTO;

public record LambdaCpEntryDTO(int index, LambdaRefDTO lambdaRef) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 7;
    }
}
