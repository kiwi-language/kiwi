package org.metavm.api.dto;

import org.jsonk.Json;

import java.util.List;

@Json
public record SchemaResponse(
        List<ClassDTO> classes
) {
}
