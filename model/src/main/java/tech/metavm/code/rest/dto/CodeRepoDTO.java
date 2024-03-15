package tech.metavm.code.rest.dto;

import tech.metavm.common.BaseDTO;

public record CodeRepoDTO(
        String id,
        Long tmpId,
        String url
) implements BaseDTO {
}
