package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Value;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.object.instance.query.TypeParsingContext;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

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

    public static ConstraintRT<?> createFromDTO(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        ClassType type = entityContext.getClassType(constraintDTO.typeId());
        ParsingContext parsingContext = new TypeParsingContext(type, entityContext.getInstanceContext());
        String message = constraintDTO.message();
        if(constraintDTO.kind() == ConstraintKind.UNIQUE.code()) {
            return createIndexConstraint(constraintDTO.getParam(), type, message, parsingContext);
        }
        if(constraintDTO.kind() == ConstraintKind.CHECK.code()) {
            return createCheckConstraint(constraintDTO.getParam(), type, message, parsingContext);
        }
        throw new InternalException("Invalid constraint kind: " + constraintDTO.kind());
    }

    public static IndexConstraintRT createIndexConstraint(UniqueConstraintParam param,
                                                          ClassType type,
                                                          String message,
                                                          ParsingContext parsingContext) {
        IndexConstraintRT index = new IndexConstraintRT(type, true, message);
        for (UniqueConstraintItemDTO itemDTO : param.items()) {
            new IndexConstraintItem(
                    index,
                    itemDTO.name(),
                    ValueFactory.getValue(itemDTO.value(), parsingContext)
            );
        }
        return index;
    }

    public static CheckConstraintRT createCheckConstraint(CheckConstraintParam param,
                                                          ClassType type,
                                                          String message,
                                                          ParsingContext parsingContext) {
        return new CheckConstraintRT(ValueFactory.getValue(param.value(), parsingContext), type, message);
    }

    public static void update(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        ConstraintRT<?> constraint = entityContext.getEntity(new TypeReference<>() {}, constraintDTO.id());
        ClassType type = entityContext.getClassType(constraintDTO.typeId());
        ParsingContext parsingContext = new TypeParsingContext(type, entityContext.getInstanceContext());

        constraint.update(constraintDTO);
        if(constraint instanceof IndexConstraintRT indexConstraint) {
            updateIndexConstraint(indexConstraint, constraintDTO.getParam(), parsingContext);
        }
        else if(constraint instanceof CheckConstraintRT checkConstraint) {
            updateCheckConstraint(checkConstraint, constraintDTO.getParam(), parsingContext);
        }
    }

    public static void updateIndexConstraint(IndexConstraintRT indexConstraint,
                                             UniqueConstraintParam param,
                                             ParsingContext parsingContext) {
        for (UniqueConstraintItemDTO itemDTO : param.items()) {
            Value value = ValueFactory.getValue(itemDTO.value(), parsingContext);
            if(itemDTO.id() == null) {
                new IndexConstraintItem(indexConstraint, itemDTO.name(), value);
            }
            else {
                IndexConstraintItem item = indexConstraint.getItem(itemDTO.id());
                item.setName(itemDTO.name());
                item.setValue(value);
            }
        }
    }

    public static void updateCheckConstraint(CheckConstraintRT checkConstraint,
                                             CheckConstraintParam param,
                                             ParsingContext parsingContext) {
        checkConstraint.setCondition(ValueFactory.getValue(param.value(), parsingContext));
    }

    public static IndexConstraintRT newUniqueConstraint(List<Field> fields) {
        NncUtils.requireNotEmpty(fields, "字段列表不能未空");
        ClassType type = fields.get(0).getDeclaringType();
        String message = "属性值'" + NncUtils.join(fields, Field::getName) + "'重复";
        return new IndexConstraintRT(type, fields, true, message);
    }

}
