package org.metavm.common.rest.dto;

import org.metavm.common.CopyContext;

public interface Copyable<T extends Copyable<T>> {

    T copy(CopyContext context);

}
