package tech.metavm.object.instance.rest;

import tech.metavm.dto.Page;
import tech.metavm.object.meta.rest.dto.TypeDTO;

import java.util.List;

public record QueryInstancesResponse(
        Page<InstanceDTO> page,
        List<TypeDTO> contextTypes
) {
}
