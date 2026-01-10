package org.manul.object.instance.rest;

import org.manul.common.Page;

public record QueryInstancesResponse(
        Page<String> page
) {
}
