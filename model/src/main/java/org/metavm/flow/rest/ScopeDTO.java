package org.metavm.flow.rest;

import org.metavm.common.rest.dto.BaseDTO;

public record ScopeDTO(
        String id,
        String codeBase64,
        int maxLocals,
        int maxStack
) implements BaseDTO {

}
