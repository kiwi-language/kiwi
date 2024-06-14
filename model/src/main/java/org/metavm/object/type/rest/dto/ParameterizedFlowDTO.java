package org.metavm.object.type.rest.dto;

import java.util.List;

public class ParameterizedFlowDTO extends GenericElementDTO {

    private final List<String> typeArgumentIds;
    private final List<GenericElementDTO> parameters;

    public ParameterizedFlowDTO(String templateRef, String ref,
                                List<String> typeArgumentIds, List<GenericElementDTO> parameters) {
        super(templateRef, ref);
        this.typeArgumentIds = typeArgumentIds;
        this.parameters = parameters;
    }

    public List<String> getTypeArgumentIds() {
        return typeArgumentIds;
    }

    public List<GenericElementDTO> getParameters() {
        return parameters;
    }
}
