package tech.metavm.expression.dto;

import tech.metavm.flow.rest.ValueDTO;

public record ConditionDTO(
        ValueDTO first,
        int opCode,
        ValueDTO second
) {
}
