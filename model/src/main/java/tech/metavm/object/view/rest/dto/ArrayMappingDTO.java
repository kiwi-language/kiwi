package tech.metavm.object.view.rest.dto;

import tech.metavm.common.RefDTO;

public record ArrayMappingDTO(
        Long id,
        Long tmpId,
        RefDTO sourceTypeRef,
        RefDTO targetTypeRef,
        RefDTO elementMappingRef
) implements MappingDTO {
    @Override
    public int getKind() {
        return 2;
    }
}
