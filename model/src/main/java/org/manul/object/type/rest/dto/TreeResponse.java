package org.manul.object.type.rest.dto;

import org.manul.object.instance.rest.TreeDTO;

import java.util.List;

public record TreeResponse(
        long version,
        List<TreeDTO> trees,
        List<Long> removedIds
) {
}
