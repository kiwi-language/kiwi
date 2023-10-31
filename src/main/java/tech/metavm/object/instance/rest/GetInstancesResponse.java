package tech.metavm.object.instance.rest;

import tech.metavm.object.meta.rest.dto.TypeDTO;

import java.util.List;

public record GetInstancesResponse(
        List<InstanceDTO> instances,
        List<TypeDTO> contextTypes
) {
}
