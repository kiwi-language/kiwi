package org.metavm.object.instance.core;

import java.util.List;

public record ScanResult(
        List<DurableInstance> instances,
        boolean completed
) {
}
