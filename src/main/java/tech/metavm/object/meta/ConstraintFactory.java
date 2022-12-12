package tech.metavm.object.meta;

import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

public class ConstraintFactory {

//    public static ConstraintRT<?> createFromPO(ConstraintPO constraintPO, EntityContext context) {
//        if(constraintPO.getKind() == ConstraintKind.UNIQUE.code()) {
//            UniqueConstraintParam param = NncUtils.readJSONString(constraintPO.getParam(), new TypeReference<>() {});
//            return new UniqueConstraintRT(constraintPO, param, context);
//        }
//        if(constraintPO.getKind() == ConstraintKind.CHECK.code()) {
//            CheckConstraintParam param = NncUtils.readJSONString(constraintPO.getParam(), new TypeReference<>() {});
//            return new CheckConstraintRT(constraintPO, param, context);
//        }
//        throw new InternalException("Invalid constraint kind: " + constraintPO.getKind());
//    }

    public static ConstraintRT<?> createFromDTO(ConstraintDTO constraintDTO, ClassType type) {
        if(constraintDTO.kind() == ConstraintKind.UNIQUE.code()) {
            UniqueConstraintParam param = constraintDTO.getParam();
            return new UniqueConstraintRT(constraintDTO, param, type);
        }
        if(constraintDTO.kind() == ConstraintKind.CHECK.code()) {
            CheckConstraintParam param = constraintDTO.getParam();
            return new CheckConstraintRT(constraintDTO, param, type);
        }
        throw new InternalException("Invalid constraint kind: " + constraintDTO.kind());
    }

    public static UniqueConstraintRT newUniqueConstraint(List<Field> fields) {
        NncUtils.requireNotEmpty(fields, "字段列表不能未空");
        ClassType type = fields.get(0).getDeclaringType();
        String message = "属性值'" + NncUtils.join(fields, Field::getName) + "'重复";
        return new UniqueConstraintRT(type, fields, message);
    }

}
