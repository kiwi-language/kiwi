package tech.metavm.entity;

import tech.metavm.object.type.rest.dto.GenericElementDTO;

public interface GenericElement {

    Object getTemplate();

    void setTemplate(Object template);

    default GenericElementDTO toGenericElementDTO(SerializeContext serializeContext) {
        return new GenericElementDTO(
                serializeContext.getRef(getTemplate()),
                serializeContext.getRef(this)
        );
    }

}
