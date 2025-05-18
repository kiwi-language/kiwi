package org.metavm.api.dto;

import java.util.List;

public record SchemaResponse(
        List<ClassDTO> classes
) {
}
