package tech.metavm.object.instance.rest;

import java.util.List;

public record ClassInstanceParamDTO(
        List<InstanceFieldDTO> fields
) {
}
