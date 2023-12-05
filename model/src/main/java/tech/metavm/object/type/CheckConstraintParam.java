package tech.metavm.object.type;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.type.rest.dto.ConstraintParam;

public record CheckConstraintParam(
        ValueDTO value
) implements ConstraintParam {

    public int getKind() {
        return ConstraintKind.CHECK.code();
    }

}
