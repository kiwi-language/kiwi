package org.metavm.object.type.rest.dto;

public record FieldCpEntryDTO(int index, FieldRefDTO fieldRef) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 2;
    }
}
