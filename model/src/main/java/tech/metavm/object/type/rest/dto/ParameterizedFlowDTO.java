package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;

public class ParameterizedFlowDTO extends GenericElementDTO {

    private final List<RefDTO> typeArgumentRefs;
    private final List<GenericElementDTO> parameters;

    public ParameterizedFlowDTO(RefDTO templateRef, RefDTO ref,
                                List<RefDTO> typeArgumentRefs, List<GenericElementDTO> parameters) {
        super(templateRef, ref);
        this.typeArgumentRefs = typeArgumentRefs;
        this.parameters = parameters;
    }

    public List<RefDTO> getTypeArgumentRefs() {
        return typeArgumentRefs;
    }

    public List<GenericElementDTO> getParameters() {
        return parameters;
    }
}
