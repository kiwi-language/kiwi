package org.metavm.code.rest.dto;

import org.metavm.common.rest.dto.BaseDTO;

public record CodeRepoDTO(
        String id,
        Long tmpId,
        String url
) implements BaseDTO {
}
