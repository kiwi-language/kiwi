package tech.metavm.view.rest.dto;

import java.util.List;

public record ListViewDTO(
        long id,
        List<Long> visibleFieldIds,
        List<Long> searchableFieldIds
) {
}
