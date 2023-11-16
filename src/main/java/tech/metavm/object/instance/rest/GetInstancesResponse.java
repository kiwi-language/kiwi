package tech.metavm.object.instance.rest;

import java.util.List;

public record GetInstancesResponse(
        List<InstanceDTO> instances
) {
}
