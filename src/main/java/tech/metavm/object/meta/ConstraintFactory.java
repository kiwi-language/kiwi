package tech.metavm.object.meta;

import com.fasterxml.jackson.core.type.TypeReference;
import tech.metavm.entity.Entity;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

public class ConstraintFactory {

    public static ConstraintRT<?> createFromPO(ConstraintPO constraintPO, Type type) {
        if(constraintPO.getKind() == ConstraintKind.UNIQUE.code()) {
            UniqueConstraintParam param = NncUtils.readJSONString(constraintPO.getParam(), new TypeReference<>() {});
            return new UniqueConstraintRT(constraintPO, param, type);
        }
        throw new InternalException("Invalid constraint kind: " + constraintPO.getKind());
    }

    public static ConstraintRT<?> createFromDTO(ConstraintDTO constraintDTO, Type type) {
        if(constraintDTO.kind() == ConstraintKind.UNIQUE.code()) {
            UniqueConstraintParam param = constraintDTO.getParam();
            return new UniqueConstraintRT(constraintDTO, param, type);
        }
        throw new InternalException("Invalid constraint kind: " + constraintDTO.kind());
    }

    public static UniqueConstraintRT newUniqueConstraint(List<Field> fields) {
        NncUtils.requireNotEmpty(fields, "字段列表不能未空");
        Type type = fields.get(0).getType();
        return (UniqueConstraintRT) createFromDTO(
                new ConstraintDTO(
                        null,
                        ConstraintKind.UNIQUE.code(),
                        type.getId(),
                        new UniqueConstraintParam(
                                NncUtils.map(fields, Entity::getId)
                        )
                ),
                type
        );
    }

}
