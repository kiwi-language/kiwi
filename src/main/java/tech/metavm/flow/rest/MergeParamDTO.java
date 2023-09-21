package tech.metavm.flow.rest;

import java.util.List;

public record MergeParamDTO(
        List<MergeFieldDTO> fields
) {
}
