package tech.metavm.object.type;

import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.Value;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.type.rest.dto.ConstraintDTO;
import tech.metavm.object.type.rest.dto.IndexFieldDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;

public class ConstraintFactory {

    public static Constraint save(ConstraintDTO constraintDTO, IEntityContext context) {
        var constraint = context.getEntity(Constraint.class, constraintDTO.getRef());
        if (constraint != null) {
            constraint.update(constraintDTO, context);
            return constraint;
        } else {
            return createFromDTO(constraintDTO, context);
        }
    }

    public static Constraint createFromDTO(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        ClassType type = entityContext.getClassType(constraintDTO.typeRef());
        ParsingContext parsingContext = TypeParsingContext.create(type, entityContext);
        String message = constraintDTO.message();
        if (constraintDTO.param() instanceof IndexParam) {
            var index = createIndexConstraint(constraintDTO.tmpId(), constraintDTO.getParam(), type,
                    constraintDTO.name(), constraintDTO.code(),
                    message, parsingContext);
            entityContext.bind(index);
            return index;
        }
        if (constraintDTO.param() instanceof CheckConstraintParam) {
            var constraint = createCheckConstraint(
                    constraintDTO.tmpId(),
                    constraintDTO.getParam(), type,
                    constraintDTO.name(), constraintDTO.code(),
                    message, parsingContext);
            entityContext.bind(constraint);
            return constraint;
        }
        throw new InternalException("Invalid constraint kind: " + constraintDTO.kind());
    }

    public static Index createIndexConstraint(
            @Nullable Long tmpId,
            IndexParam param,
            ClassType type,
            String name,
            @Nullable String code,
            String message,
            ParsingContext parsingContext) {
        var index = new Index(type, name, code, message, param.unique());
        index.setTmpId(tmpId);
        for (IndexFieldDTO fieldDTO : param.fields()) {
            var indexField = new IndexField(
                    index,
                    fieldDTO.name(),
                    fieldDTO.code(),
                    ValueFactory.create(fieldDTO.value(), parsingContext)
            );
            indexField.setTmpId(fieldDTO.tmpId());
        }
        return index;
    }

    public static CheckConstraint createCheckConstraint(
            @Nullable Long tmpId,
            CheckConstraintParam param,
            ClassType type,
            String name,
            @Nullable String code,
            String message,
            ParsingContext parsingContext) {
        var constraint = new CheckConstraint(type, name, code, message, ValueFactory.create(param.value(), parsingContext));
        constraint.setTmpId(tmpId);
        return constraint;
    }

    public static void update(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        Constraint constraint = entityContext.getEntity(new TypeReference<>() {
        }, constraintDTO.id());
        ClassType type = entityContext.getClassType(constraintDTO.typeRef());
        ParsingContext parsingContext = TypeParsingContext.create(type, entityContext);
        constraint.update(constraintDTO, entityContext);
        if (constraint instanceof Index indexConstraint) {
            updateIndex(indexConstraint, constraintDTO.getParam(), parsingContext);
        } else if (constraint instanceof CheckConstraint checkConstraint) {
            updateCheckConstraint(checkConstraint, constraintDTO.getParam(), parsingContext);
        }
    }

    public static void updateIndex(Index index,
                                   IndexParam param,
                                   ParsingContext parsingContext) {
        for (IndexFieldDTO fieldDTO : param.fields()) {
            Value value = ValueFactory.create(fieldDTO.value(), parsingContext);
            if (fieldDTO.id() == null) {
                new IndexField(index, fieldDTO.name(), fieldDTO.code(), value);
            } else {
                IndexField item = index.getField(fieldDTO.id());
                item.setName(fieldDTO.name());
                item.setCode(fieldDTO.code());
                item.setValue(value);
            }
        }
    }

    public static void updateCheckConstraint(CheckConstraint checkConstraint,
                                             CheckConstraintParam param,
                                             ParsingContext parsingContext) {
        checkConstraint.setCondition(ValueFactory.create(param.value(), parsingContext));
    }

    public static Index newUniqueConstraint(String name, @Nullable String code, List<Field> fields) {
        NncUtils.requireNotEmpty(fields, "字段列表不能未空");
        ClassType type = fields.get(0).getDeclaringType();
        String message = "属性值'" + NncUtils.join(fields, Field::getName) + "'重复";
        return new Index(type, name, code, message, true, fields);
    }

}
