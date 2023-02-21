package tech.metavm.object.instance.rest;

import java.util.List;

public record BatchGetInstancesRequest(
        List<Long> ids,
        int depth
) {
}
