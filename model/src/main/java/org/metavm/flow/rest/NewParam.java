package org.metavm.flow.rest;

public interface NewParam<T extends NewParam<T>> {

    T copyWithParentRef(ParentRefDTO parentRef);

}
