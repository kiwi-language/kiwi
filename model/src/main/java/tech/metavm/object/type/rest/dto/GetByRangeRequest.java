package tech.metavm.object.type.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record GetByRangeRequest(
        String lowerBoundId,
        String upperBoundId,
        boolean includeParameterized,
        boolean isTemplate,
        boolean includeBuiltin,
        @Nullable List<Integer> categories
) {
}
