package org.metavm.expression.dto;

import java.util.List;

public record ConditionGroupDTO (
        List<ConditionDTO> items
) {
}
