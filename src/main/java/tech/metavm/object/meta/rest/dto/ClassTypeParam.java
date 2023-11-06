package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.rest.InstanceDTO;

import javax.annotation.Nullable;
import java.util.List;

public record ClassTypeParam(
        RefDTO superClassRef,
        List<RefDTO> interfaceRefs,
        int source,
        List<FieldDTO> fields,
        List<FieldDTO> staticFields,
        List<ConstraintDTO> constraints,
        List<FlowDTO> flows,
        String desc,
        Object extra,
        List<InstanceDTO> enumConstants,
        boolean isTemplate,
        List<RefDTO> typeParameterRefs,
        @Nullable List<TypeDTO> typeParameters,
        RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<RefDTO> dependencyRefs,
        boolean hasSubTypes
) implements TypeParam {

    @Override
    public int getType() {
        return 1;
    }
}
