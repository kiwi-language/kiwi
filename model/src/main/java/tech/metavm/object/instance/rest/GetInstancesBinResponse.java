package tech.metavm.object.instance.rest;

import java.util.List;
import java.util.Map;

public record GetInstancesBinResponse(
        List<TreeDTO> trees,
        Map<Long, Long> invertedIndex
) {
}
