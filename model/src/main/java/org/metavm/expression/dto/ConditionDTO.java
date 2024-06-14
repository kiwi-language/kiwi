package org.metavm.expression.dto;

import org.metavm.flow.rest.ValueDTO;

public record ConditionDTO(
        ValueDTO first,
        int opCode,
        ValueDTO second
) {
}
