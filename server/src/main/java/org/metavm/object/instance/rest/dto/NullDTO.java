package org.metavm.object.instance.rest.dto;

public final class NullDTO implements ValueDTO {

    public static final NullDTO instance = new NullDTO();

    @Override
    public String getKind() {
        return "null";
    }
}
