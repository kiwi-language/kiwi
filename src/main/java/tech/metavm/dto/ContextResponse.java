package tech.metavm.dto;

import tech.metavm.object.meta.rest.dto.TypeDTO;

import java.util.List;

public class ContextResponse {
    private final List<TypeDTO> contextTypes;

    public ContextResponse(List<TypeDTO> contextTypes) {
        this.contextTypes = contextTypes;
    }
}
