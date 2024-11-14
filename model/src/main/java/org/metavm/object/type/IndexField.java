package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.Expression;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.ThisExpression;
import org.metavm.flow.Values;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.rest.dto.IndexFieldDTO;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

@EntityType
public class IndexField extends Entity implements LocalKey {

    public static IndexField createFieldItem(Index constraint, Field field) {
        return new IndexField(
                constraint,
                field.getName(),
                Values.property(field)
        );
    }

    private final Index index;
    private String name;
    private org.metavm.flow.Value value;

    public IndexField(Index index, String name, org.metavm.flow.Value value) {
        setName(name);
        this.index = index;
        this.value = value;
        index.addField(this);
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return index.getDeclaringType().getName() + "." + index.getName() + "." + name;
    }

    public org.metavm.flow.Value getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(org.metavm.flow.Value value) {
        this.value = value;
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
                    value.toDTO()
            );
        }
    }

    public int getFieldIndex() {
        return index.getFieldIndex(this);
    }

    public Value convertEntityToInstance(Object entity, IEntityContext context) {
        if (Instances.isPrimitive(entity) || context.containsEntity(entity))
            return context.getObjectInstanceMap().getInstance(entity);
        else
            throw new InternalException("Entity " + entity + " not found in the context");

    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }
}
