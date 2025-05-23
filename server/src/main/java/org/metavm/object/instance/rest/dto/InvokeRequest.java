package org.metavm.object.instance.rest.dto;

import java.util.List;

public record InvokeRequest(
        ValueDTO receiver,
        String method,
        List<ArgumentDTO> arguments
) {
}
