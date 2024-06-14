package org.metavm.flow.rest;

import java.util.List;

public record MergeNodeParam(
        List<MergeFieldDTO> fields
) {
}
