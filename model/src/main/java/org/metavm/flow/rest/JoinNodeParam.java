package org.metavm.flow.rest;

import java.util.List;

public record JoinNodeParam(
        List<String> sourceIds,
        List<JoinNodeFieldDTO> fields
) {
}
