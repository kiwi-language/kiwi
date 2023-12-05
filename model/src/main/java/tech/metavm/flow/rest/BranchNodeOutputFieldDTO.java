package tech.metavm.flow.rest;

import java.util.List;

public record BranchNodeOutputFieldDTO(
        Long fieldId,
        String fieldCode,
        List<ConditionalValueDTO> values
) {
}
