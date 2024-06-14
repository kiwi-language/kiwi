package org.metavm.code.rest.dto;

import org.metavm.common.BaseDTO;

public record CodeRepoDTO(
        String id,
        Long tmpId,
        String url
) implements BaseDTO {
}
