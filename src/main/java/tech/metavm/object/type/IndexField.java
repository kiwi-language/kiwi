package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.expression.ThisExpression;
import tech.metavm.flow.ExpressionValue;
import tech.metavm.flow.ReferenceValue;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("唯一约束项")
public class IndexField extends Entity {

    public static IndexField createFieldItem(Index constraint, Field field) {
        return new IndexField(
                constraint,
                field.getName(),
                field.getCode(),
                new ReferenceValue(ExpressionUtil.attributeExpr(field))
        );
    }

    @EntityField("唯一约束")
    private final Index index;
    @EntityField("名称")
    private String name;
    @EntityField(value = "编号", asKey = true)
    @Nullable
    private String code;
    @ChildEntity("值")
    private Value value;

    public IndexField(Index index, String name, @Nullable String code, Value value) {
        setName(name);
        setCode(code);
        this.index = index;
        setValue(value);// ;ValueFactory.getValue(valueDTO, new TypeParsingContext(constraint.getDeclaringType())));
        index.addField(this);
    }

    public String getName() {
        return name;
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

    public Field getField() {
        Expression expression = null;
        if (value instanceof ReferenceValue referenceValue) {
            expression = referenceValue.getExpression();
        }
        if (value instanceof ExpressionValue expressionValue) {
            expression = expressionValue.getExpression();
        }
        if (expression != null) {
            return extractField(expression);
        }
        return null;
    }

    private Field extractField(Expression expression) {
        if (expression instanceof PropertyExpression fieldExpression) {
            if ((fieldExpression.getInstance() instanceof ThisExpression)) {
                return (Field) fieldExpression.getProperty();
            }
        }
        return null;
    }

    public IndexFieldDTO toDTO(boolean forPersistence) {
        return new IndexFieldDTO(
                id,
                name,
                code,
                value.toDTO(forPersistence)
        );
    }

    public void setKeyItem(IndexKeyPO key, Instance fieldValue) {
        if (isColumnX()) {
            if (fieldValue.isNull()) {
                key.setColumnXPresent(false);
                key.setColumnX(0L);
            } else {
                key.setColumnXPresent(true);
                if (fieldValue instanceof LongInstance longInstance) {
                    key.setColumnX(longInstance.getValue());
                } else {
                    throw new InternalException("Can not set column X with " + fieldValue);
                }
            }
        } else {
            int index = this.index.getItemIndex(this);
            Object indexValue;
            if (fieldValue.isNull()) {
                indexValue = null;
            } else if (fieldValue instanceof PrimitiveInstance primitiveInstance) {
                indexValue = primitiveInstance.getValue();
            } else {
                indexValue = NncUtils.requireNonNull(fieldValue.getId());
            }
            key.setColumn(index, IndexKeyPO.getIndexColumn(indexValue));
        }
    }

    public boolean isItemNull(IndexKeyPO key) {
        if (isColumnX()) {
            return key.isColumnXPresent();
        } else {
            return key.getColumn(index.getItemIndex(this)).equals(IndexKeyPO.NULL);
        }
    }

    public boolean isColumnX() {
        return (value.getType().isLong() || value.getType().isInt()) && index.isLastItem(this);
    }

    public Instance convertModelToInstance(Object model, IEntityContext context) {
        if (InstanceUtils.isPrimitiveEntityValue(model) || context.containsModel(model))
            return context.getInstance(model);
        else
            throw new InternalException("Model " + model + " does not exist in the context");

    }

}
