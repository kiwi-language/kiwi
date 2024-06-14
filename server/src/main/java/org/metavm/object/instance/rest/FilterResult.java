package org.metavm.object.instance.rest;

import java.util.List;

public record FilterResult(
        boolean successful,
        String message,
        List<InstanceDTO> data,
        int total
) {
}
