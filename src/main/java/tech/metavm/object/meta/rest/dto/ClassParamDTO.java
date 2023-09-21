package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public record ClassParamDTO(
        Long superTypeId,
        TypeDTO superType,
        List<RefDTO> interfaceRefs,
        int source,
        List<FieldDTO> fields,
        List<FieldDTO> staticFields,
        List<ConstraintDTO> constraints,
        List<FlowDTO> flows,
        String template,
        String desc,
        Object extra,
        List<EnumConstantDTO> enumConstants,
        List<RefDTO> typeArgumentRefs
) {

}
