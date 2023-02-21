package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;

import java.util.List;

public record ClassInstanceParamDTO(
        List<InstanceFieldDTO> fields
) implements InstanceParamDTO {

    @Override
    public int getType() {
        return InstanceKind.CLASS.code();
    }

}
