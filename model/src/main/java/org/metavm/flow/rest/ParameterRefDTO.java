package org.metavm.flow.rest;

public record ParameterRefDTO(
        CallableRefDTO callableRef,
        String rawParameterId
) {
}
