package tech.metavm.object.instance.rest;

import java.util.List;

public record GetInstancesRequest(
        List<Long> ids,
        int depth
) {
}
