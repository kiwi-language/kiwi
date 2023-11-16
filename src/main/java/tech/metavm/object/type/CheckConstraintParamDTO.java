package tech.metavm.object.type;

import tech.metavm.flow.rest.ValueDTO;

public record CheckConstraintParamDTO(
        ValueDTO value
) {

    public int getKind() {
        return ConstraintKind.CHECK.code();
    }

}
