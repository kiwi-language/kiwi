package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import java.util.List;

public record UpdateStaticNodeParam(
        RefDTO typeRef,
        List<UpdateFieldDTO> fields
) {
}
