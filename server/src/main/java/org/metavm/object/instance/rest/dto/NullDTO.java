package org.metavm.object.instance.rest.dto;

public final class NullDTO implements ValueDTO {
    @Override
    public String getKind() {
        return "null";
    }
}
