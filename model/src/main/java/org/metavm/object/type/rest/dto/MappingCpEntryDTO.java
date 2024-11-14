package org.metavm.object.type.rest.dto;

import org.metavm.object.view.rest.dto.ObjectMappingRefDTO;

public record MappingCpEntryDTO(int index, ObjectMappingRefDTO mappingRef) implements CpEntryDTO {
    @Override
    public int getKind() {
        return 5;
    }
}
