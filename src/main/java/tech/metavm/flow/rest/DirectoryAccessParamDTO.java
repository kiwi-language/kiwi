package tech.metavm.flow.rest;

import java.util.List;

public record DirectoryAccessParamDTO(
        long directoryId,
        List<FieldParamDTO> fieldParams
) {
}
