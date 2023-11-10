package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;

import java.util.List;
import java.util.Map;

public record ParameterizedTypeDTO(
        Long id,
        Long tmpId,
        String name,
        RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        Map<String, RefDTO> propertyRefs,
        List<RefDTO> dependencyRefs
) implements BaseDTO {

}
