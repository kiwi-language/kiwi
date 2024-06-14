package org.metavm.flow.rest;

import java.util.List;

public record FlowSignatureDTO(
        String name,
        List<String> parameterTypeIds
) {
}
