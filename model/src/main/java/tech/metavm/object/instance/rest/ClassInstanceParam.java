package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;
import tech.metavm.util.NncUtils;

import java.io.Serializable;
import java.util.*;

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

    @Override
    public boolean valueEquals(InstanceParam param1, Set<String> newIds) {
        if (param1 instanceof ClassInstanceParam param2 && fields.size() == param2.fields.size()) {
            var fields1 = fields.stream().sorted(Comparator.comparingLong(InstanceFieldDTO::fieldId)).toList();
            var fields2 = param2.fields.stream().sorted(Comparator.comparingLong(InstanceFieldDTO::fieldId)).toList();
            return NncUtils.listEquals(fields1, fields2, (fieldDTO, that) -> fieldDTO.valueEquals(that, newIds));
        } else
            return false;
    }
}
