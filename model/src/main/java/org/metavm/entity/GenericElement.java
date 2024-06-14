package org.metavm.entity;

import org.metavm.util.NncUtils;

public interface GenericElement {

    default Object getSelfOrCopySource() {
        return NncUtils.getOrElse(getCopySource(), s -> s, this);
    }

    Object getCopySource();

    void setCopySource(Object copySource);

}
