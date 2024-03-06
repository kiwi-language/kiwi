package tech.metavm.object.type.rest.dto;

import tech.metavm.common.ErrorDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FlowSignatureDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public record ClassTypeParam(
        RefDTO superClassRef,
        List<RefDTO> interfaceRefs,
        int source,
        List<FieldDTO> fields,
        List<FieldDTO> staticFields,
        @Nullable RefDTO titleFieldRef,
        List<ConstraintDTO> constraints,
        List<FlowDTO> flows,
        List<ObjectMappingDTO> mappings,
        @Nullable RefDTO defaultMappingRef,
        String desc,
        Object extra,
        List<InstanceDTO> enumConstants,
        boolean isAbstract,
        boolean isTemplate,
        List<RefDTO> typeParameterRefs,
        @Nullable List<TypeDTO> typeParameters,
        RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<RefDTO> dependencyRefs,
        boolean hasSubTypes,
        boolean struct,
        List<ErrorDTO> errors
) implements TypeParam {

    @Override
    public int getType() {
        return 1;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public TypeKey getTypeKey() {
        return templateRef != null ? new ParameterizedTypeKey(templateRef, typeArgumentRefs) : null;
    }

    public FieldDTO findFieldByName(String name) {
        var field = NncUtils.find(fields, f -> f.name().equals(name));
        if(field != null)
            return field;
        return NncUtils.find(staticFields, f -> f.name().equals(name));
    }

    public FlowDTO findFlowBySignature(FlowSignatureDTO signature) {
        return NncUtils.find(flows, f -> f.signature().equals(signature));
    }

}
