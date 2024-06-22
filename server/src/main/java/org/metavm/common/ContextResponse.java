package org.metavm.common;

import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.List;

public class ContextResponse {
    private final List<KlassDTO> contextTypes;

    public ContextResponse(List<KlassDTO> contextTypes) {
        this.contextTypes = contextTypes;
    }
}
