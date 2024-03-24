package tech.metavm.object.instance.rest;

import java.util.List;

public record InstanceVersionsRequest(
        List<Long> ids
) {
}
