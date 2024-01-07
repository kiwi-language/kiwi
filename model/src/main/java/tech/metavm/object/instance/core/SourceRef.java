package tech.metavm.object.instance.core;

import tech.metavm.object.view.Mapping;

public record SourceRef(
        DurableInstance source,
        Mapping mapping
) {
}
