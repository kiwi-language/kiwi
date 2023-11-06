package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Value;
import tech.metavm.flow.ValueFactory;
import tech.metavm.expression.ParsingContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.util.List;
import java.util.Objects;

public class ConstraintFactory {

    public static Constraint save(ConstraintDTO constraintDTO, IEntityContext context) {
        var constraint = context.getEntity(Constraint.class, constraintDTO.getRef());
        if(constraint != null) {
            constraint.update(constraintDTO);
            return constraint;
        }
        else {
            return createFromDTO(constraintDTO, context);
        }
    }

    public static Constraint createFromDTO(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        ClassType type = entityContext.getClassType(constraintDTO.typeId());
        ParsingContext parsingContext = new TypeParsingContext(type,
                Objects.requireNonNull(entityContext.getInstanceContext()));
        String message = constraintDTO.message();
        if(constraintDTO.kind() == ConstraintKind.UNIQUE.code()) {
            return createIndexConstraint(constraintDTO.getParam(), type, message, parsingContext);
        }
        if(constraintDTO.kind() == ConstraintKind.CHECK.code()) {
            return createCheckConstraint(constraintDTO.getParam(), type, message, parsingContext);
        }
        throw new InternalException("Invalid constraint kind: " + constraintDTO.kind());
    }

    public static Index createIndexConstraint(UniqueConstraintParamDTO param,
                                              ClassType type,
                                              String message,
                                              ParsingContext parsingContext) {
        Index index = new Index(type, true, message);
        for (UniqueConstraintItemDTO itemDTO : param.items()) {
            new IndexField(
                    index,
                    itemDTO.name(),
                    ValueFactory.create(itemDTO.value(), parsingContext)
            );
        }
        return index;
    }

    public static CheckConstraint createCheckConstraint(CheckConstraintParamDTO param,
                                                        ClassType type,
                                                        String message,
                                                        ParsingContext parsingContext) {
        return new CheckConstraint(ValueFactory.create(param.value(), parsingContext), type, message);
    }

    public static void update(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        Constraint constraint = entityContext.getEntity(new TypeReference<>() {}, constraintDTO.id());
        ClassType type = entityContext.getClassType(constraintDTO.typeId());
        ParsingContext parsingContext = new TypeParsingContext(type,
                Objects.requireNonNull(entityContext.getInstanceContext()));
        constraint.update(constraintDTO);
        if(constraint instanceof Index indexConstraint) {
            updateIndexConstraint(indexConstraint, constraintDTO.getParam(), parsingContext);
        }
        else if(constraint instanceof CheckConstraint checkConstraint) {
            updateCheckConstraint(checkConstraint, constraintDTO.getParam(), parsingContext);
        }
    }

    public static void updateIndexConstraint(Index indexConstraint,
                                             UniqueConstraintParamDTO param,
                                             ParsingContext parsingContext) {
        for (UniqueConstraintItemDTO itemDTO : param.items()) {
            Value value = ValueFactory.create(itemDTO.value(), parsingContext);
            if(itemDTO.id() == null) {
                new IndexField(indexConstraint, itemDTO.name(), value);
            }
            else {
                IndexField item = indexConstraint.getField(itemDTO.id());
                item.setName(itemDTO.name());
                item.setValue(value);
            }
        }
    }

    public static void updateCheckConstraint(CheckConstraint checkConstraint,
                                             CheckConstraintParamDTO param,
                                             ParsingContext parsingContext) {
        checkConstraint.setCondition(ValueFactory.create(param.value(), parsingContext));
    }

    public static Index newUniqueConstraint(List<Field> fields) {
        NncUtils.requireNotEmpty(fields, "字段列表不能未空");
        ClassType type = fields.get(0).getDeclaringType();
        String message = "属性值'" + NncUtils.join(fields, Field::getName) + "'重复";
        return new Index(type, fields, true, message);
    }

}
