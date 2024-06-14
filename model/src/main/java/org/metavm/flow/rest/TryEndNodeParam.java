package org.metavm.flow.rest;

import java.util.List;

public record TryEndNodeParam(
        List<TryEndFieldDTO> fields
) {
}
