package org.metavm.common;

import org.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public class ContextResponse {
    private final List<TypeDTO> contextTypes;

    public ContextResponse(List<TypeDTO> contextTypes) {
        this.contextTypes = contextTypes;
    }
}
