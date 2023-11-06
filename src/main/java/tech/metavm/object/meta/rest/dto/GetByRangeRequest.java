package tech.metavm.object.meta.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record GetByRangeRequest(
        long lowerBoundId,
        long upperBoundId,
        boolean isParameterized,
        boolean isTemplate,
        boolean includeBuiltin,
        @Nullable List<Integer> categories
) {
}
