package org.manul.util;

import org.manul.object.instance.core.ApiObject;

import java.util.List;

public record ApiSearchResult(
        List<ApiObject> items,
        long total
) {
}
