package org.metavm.object.type.rest.dto;

import org.metavm.common.RefDTO;

import java.util.List;

public class ClassTypeRefDTO extends TypeRefDTO {

    private final RefDTO templateRef;
    private final List<RefDTO> typeArgumentRefs;

    public ClassTypeRefDTO(int category, long id, String name, RefDTO templateRef, List<RefDTO> typeArgumentRefs) {
        super(category, id, name);
        this.templateRef = templateRef;
        this.typeArgumentRefs = typeArgumentRefs;
    }

    public RefDTO getTemplateRef() {
        return templateRef;
    }

    public List<RefDTO> getTypeArgumentRefs() {
        return typeArgumentRefs;
    }
}
