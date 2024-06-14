package org.metavm.view.rest.dto;

import java.util.List;

public record ListViewDTO(
        String id,
        List<String> visibleFieldIds,
        List<String> searchableFieldIds
) {
}
