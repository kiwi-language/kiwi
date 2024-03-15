package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.rest.TreeDTO;

import java.util.List;

public record TreeResponse(
        long version,
        List<TreeDTO> trees,
        List<String> removedIds
) {
}
