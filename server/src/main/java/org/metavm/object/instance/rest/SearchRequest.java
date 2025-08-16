package org.metavm.object.instance.rest;

import javax.annotation.Nullable;
import java.util.Map;

public record SearchRequest(
        long appId,
        String type,
        Map<String, Object> criteria,
        @Nullable String newlyCreatedId,
        int page,
        int pageSize
) {
}
