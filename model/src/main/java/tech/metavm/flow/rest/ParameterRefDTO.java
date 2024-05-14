package tech.metavm.flow.rest;

public record ParameterRefDTO(
        CallableRefDTO callableRef,
        String rawParameterId
) {
}
