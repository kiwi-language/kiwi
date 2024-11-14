package org.metavm.object.type.rest.dto;

public record TypeCpEntryDTO(int index, String type) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 1;
    }
}
