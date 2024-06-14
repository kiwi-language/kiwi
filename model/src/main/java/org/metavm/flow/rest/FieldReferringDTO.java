package org.metavm.flow.rest;

public interface FieldReferringDTO<T extends FieldReferringDTO<T>> {

    String fieldId();

    T copyWithFieldId(String fieldId);

}
