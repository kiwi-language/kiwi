package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;

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
