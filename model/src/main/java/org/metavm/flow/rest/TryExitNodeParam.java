package org.metavm.flow.rest;

import java.util.List;

public record TryExitNodeParam(
        List<TryExitFieldDTO> fields
) {
}
