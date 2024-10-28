package org.metavm.object.type;

import org.metavm.entity.IEntityContext;
import org.metavm.expression.ParsingContext;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.Value;
import org.metavm.flow.ValueFactory;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.CheckConstraintParam;
import org.metavm.object.type.rest.dto.IndexParam;
import org.metavm.object.type.rest.dto.ConstraintDTO;
import org.metavm.object.type.rest.dto.IndexFieldDTO;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;

public class ConstraintFactory {

    public static Constraint save(ConstraintDTO constraintDTO, IEntityContext context) {
        var constraint = context.getEntity(Constraint.class, constraintDTO.id());
        if (constraint != null) {
            constraint.update(constraintDTO, context);
            return constraint;
        } else {
            return createFromDTO(constraintDTO, context);
        }
    }

    public static Constraint createFromDTO(ConstraintDTO constraintDTO, IEntityContext entityContext) {
        Klass type = entityContext.getKlass(constraintDTO.typeId());
        ParsingContext parsingContext = TypeParsingContext.create(type, entityContext);
        String message = constraintDTO.message();
        if (constraintDTO.param() instanceof IndexParam) {
            var index = createIndexConstraint(constraintDTO.tmpId(), constraintDTO.getParam(), type,
                    constraintDTO.name(), constraintDTO.code(),
                    message, parsingContext, entityContext);
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
            Klass type,
            String name,
            @Nullable String code,
            String message,
            ParsingContext parsingContext,
            IEntityContext entityContext) {
        var method =  NncUtils.get(param.methodId(), entityContext::getMethod);
        var index = new Index(type, name, code, message, param.unique(), List.of(), method);
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
            Klass type,
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
        Klass type = entityContext.getKlass(constraintDTO.typeId());
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
                IndexField item = index.getField(Id.parse(fieldDTO.id()));
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
        NncUtils.requireNotEmpty(fields, "fields can not empty");
        Klass type = fields.get(0).getDeclaringType();
        String message = "Duplicate field '" + NncUtils.join(fields, Field::getQualifiedName) + "'";
        return new Index(type, name, code, message, true, fields, null);
    }

}
