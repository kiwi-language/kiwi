package tech.metavm.object.instance.rest;

import tech.metavm.common.Page;
import tech.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public record QueryInstancesResponse(
        Page<InstanceDTO> page,
        List<TypeDTO> contextTypes
) {
}
