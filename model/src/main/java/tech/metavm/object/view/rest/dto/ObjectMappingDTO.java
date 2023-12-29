package tech.metavm.object.view.rest.dto;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public record ObjectMappingDTO(
        Long id,
        Long tmpId,
        String name,
        @Nullable String code,
        RefDTO sourceTypeRef,
        RefDTO targetTypeRef,
        boolean isDefault,
        boolean builtin,
        List<RefDTO> overriddenRefs,
        ObjectMappingParam param
) implements MappingDTO {
    @Override
    public int getKind() {
        return 1;
    }
}
