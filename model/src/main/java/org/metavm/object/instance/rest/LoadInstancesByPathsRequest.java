package org.metavm.object.instance.rest;

import java.util.List;

public record LoadInstancesByPathsRequest(
        Long thisId,
        List<String> paths
) {
}
