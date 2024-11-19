package org.metavm.object.instance.rest;

import org.metavm.common.Page;

public record QueryInstancesResponse(
        Page<String> page
) {
}
