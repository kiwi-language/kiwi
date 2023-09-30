package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.rest.FlowDTO;

import java.util.List;

public record ClassParamDTO(
        RefDTO superTypeRef,
        TypeDTO superType,
        List<TypeDTO> interfaces,
        List<RefDTO> interfaceRefs,
        int source,
        List<FieldDTO> fields,
        List<FieldDTO> staticFields,
        List<ConstraintDTO> constraints,
        List<FlowDTO> flows,
        String templateName,
        String desc,
        Object extra,
        List<EnumConstantDTO> enumConstants,
        List<TypeDTO> typeParameters,
        RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<RefDTO> dependencyRefs
) {

}
