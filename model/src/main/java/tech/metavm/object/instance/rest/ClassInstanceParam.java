package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;
import tech.metavm.util.NncUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record ClassInstanceParam(
        List<InstanceFieldDTO> fields
) implements InstanceParam, Serializable {

    @Override
    public int getType() {
        return InstanceKind.CLASS.code();
    }

    public ClassInstanceParam copyWithNewField(InstanceFieldDTO fieldDTO) {
        var oldFields = NncUtils.filter(fields, f -> !Objects.equals(f.fieldId(), fieldDTO.fieldId()));
        return new ClassInstanceParam(NncUtils.append(oldFields, fieldDTO));
    }

}
