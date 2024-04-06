package tech.metavm.entity;

import tech.metavm.object.type.rest.dto.GenericElementDTO;
import tech.metavm.util.NncUtils;

public interface GenericElement {

    default Object getSelfOrCopySource() {
        return NncUtils.getOrElse(getCopySource(), s -> s, this);
    }

    Object getCopySource();

    void setCopySource(Object copySource);

    default GenericElementDTO toGenericElementDTO(SerializeContext serializeContext) {
        return new GenericElementDTO(
                serializeContext.getId(getCopySource()),
                serializeContext.getId(this)
        );
    }

}
