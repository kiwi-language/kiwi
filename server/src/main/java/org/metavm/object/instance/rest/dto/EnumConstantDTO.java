package org.metavm.object.instance.rest.dto;

public record EnumConstantDTO(String type, String name) implements ValueDTO {
    @Override
    public String getKind() {
        return "enumConstant";
    }
}
