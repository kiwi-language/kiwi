package org.metavm.flow.rest;

import java.util.List;

public record UpdateStaticNodeParam(
        String typeId,
        List<UpdateFieldDTO> fields
) {
}
