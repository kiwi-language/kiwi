package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public interface FieldReferringDTO<T extends FieldReferringDTO<T>> {

    RefDTO fieldRef();

    T copyWithFieldRef(RefDTO fieldRef);

}
