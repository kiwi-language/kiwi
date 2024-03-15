package tech.metavm.object.view.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record ObjectMappingDTO(
        String id,
        String name,
        @Nullable String code,
        String sourceTypeId,
        String targetTypeId,
        boolean isDefault,
        boolean builtin,
        List<String> overriddenIds,
        ObjectMappingParam param
) implements MappingDTO {
    @Override
    public int getKind() {
        return 1;
    }
}
