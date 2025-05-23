package org.metavm.object.instance.rest;

import org.metavm.object.instance.rest.dto.ObjectDTO;

import java.util.List;

public record SearchResult(
        List<ObjectDTO> items,
        long total
) {
}
