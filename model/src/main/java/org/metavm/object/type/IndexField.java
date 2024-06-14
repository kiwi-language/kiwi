package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.*;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.ThisExpression;
import org.metavm.flow.Value;
import org.metavm.flow.Values;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.rest.dto.IndexFieldDTO;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class IndexField extends Entity implements LocalKey {

    public static IndexField createFieldItem(Index constraint, Field field) {
        return new IndexField(
                constraint,
                field.getName(),
                field.getCode(),
                Values.reference(Expressions.propertyExpr(field))
        );
    }

    private final Index index;
    private String name;
    @EntityField(asKey = true)
    @Nullable
    private String code;
    private Value value;

    public IndexField(Index index, String name, @Nullable String code, Value value) {
        setName(name);
        setCode(code);
        this.index = index;
        this.value = value;
        index.addField(this);
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public Value getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public Index getIndex() {
        return index;
    }

    public Field getField() {
        return extractField(value.getExpression());
    }

    private Field extractField(Expression expression) {
        if (expression instanceof PropertyExpression fieldExpression) {
            if ((fieldExpression.getInstance() instanceof ThisExpression)) {
                return (Field) fieldExpression.getProperty();
            }
        }
        return null;
    }

    public IndexFieldDTO toDTO() {
        try(var serializeContext = SerializeContext.enter()) {
            return new IndexFieldDTO(
                    serializeContext.getStringId(this),
                    name,
                    code,
                    value.toDTO()
            );
        }
    }

    public int getFieldIndex() {
        return index.getFieldIndex(this);
    }

    public Instance convertEntityToInstance(Object entity, IEntityContext context) {
        if (Instances.isPrimitive(entity) || context.containsEntity(entity))
            return context.getObjectInstanceMap().getInstance(entity);
        else
            throw new InternalException("Entity " + entity + " not found in the context");

    }

    @Override
    public boolean isValidLocalKey() {
        return code != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(code);
    }
}
