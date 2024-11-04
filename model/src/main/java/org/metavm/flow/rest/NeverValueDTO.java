package org.metavm.flow.rest;

public record NeverValueDTO() implements ValueDTO {
    @Override
    public int getKind() {
        return ValueKindCodes.NEVER;
    }
}
