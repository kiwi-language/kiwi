package org.metavm.object.instance.rest.dto;

public record BeanDTO(String name) implements ValueDTO {
    @Override
    public String getKind() {
        return "bean";
    }
}
