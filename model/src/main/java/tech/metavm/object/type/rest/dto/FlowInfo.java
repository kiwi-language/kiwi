package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;
import java.util.function.Consumer;

public class FlowInfo extends GenericElementDTO {

    private final List<GenericElementDTO> parameters;

    private final List<GenericElementDTO> typeVariables;

    public FlowInfo(RefDTO templateRef, RefDTO ref, List<GenericElementDTO> parameters, List<GenericElementDTO> typeVariables) {
        super(templateRef, ref);
        this.parameters = parameters;
        this.typeVariables = typeVariables;
    }

    public List<GenericElementDTO> getParameters() {
        return parameters;
    }

    public List<GenericElementDTO> getTypeVariables() {
        return typeVariables;
    }

    @Override
    public void forEachDescendant(Consumer<GenericElementDTO> action) {
        super.forEachDescendant(action);
        parameters.forEach(p -> p.forEachDescendant(action));
        typeVariables.forEach(tv -> tv.forEachDescendant(action));
    }

}
