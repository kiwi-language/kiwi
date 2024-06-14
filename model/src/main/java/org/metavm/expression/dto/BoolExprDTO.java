package org.metavm.expression.dto;

import java.util.List;

public record BoolExprDTO (
        List<ConditionGroupDTO> conditionGroups
) {

}
