package org.metavm.object.type.rest.dto;

public record IndexCpEntryDTO(int index, IndexRefDTO indexRef) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 6;
    }
}
