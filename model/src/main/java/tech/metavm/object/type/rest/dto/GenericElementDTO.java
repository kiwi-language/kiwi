package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.function.Consumer;

public class GenericElementDTO {
    private final RefDTO templateRef;
    private final RefDTO ref;

    public GenericElementDTO(RefDTO templateRef, RefDTO ref) {
        this.templateRef = templateRef;
        this.ref = ref;
    }

    public RefDTO getTemplateRef() {
        return templateRef;
    }

    public RefDTO getRef() {
        return ref;
    }

    public void forEachDescendant(Consumer<GenericElementDTO> action) {
        action.accept(this);
    }

}
