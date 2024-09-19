package org.metavm.flow.rest;

import java.util.List;

public record TargetNodeParam(
        List<String> sourceIds
) {
}
