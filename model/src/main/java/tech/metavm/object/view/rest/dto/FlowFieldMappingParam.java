package tech.metavm.object.view.rest.dto;

import javax.annotation.Nullable;

public record FlowFieldMappingParam(
        String getterId,
        @Nullable String setterId
) implements FieldMappingParam{

    @Override
    public int getKind() {
        return 2;
    }
}
