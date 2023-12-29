package tech.metavm.object.view.rest.dto;

import java.util.List;

public record InstanceFormDTO(
        Long id,
        List<InstanceFormFieldDTO> fields
) {
}
