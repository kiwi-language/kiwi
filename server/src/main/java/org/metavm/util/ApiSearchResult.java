package org.metavm.util;

import org.metavm.object.instance.core.ApiObject;

import java.util.List;

public record ApiSearchResult(
        List<ApiObject> data,
        long total
) {
}
