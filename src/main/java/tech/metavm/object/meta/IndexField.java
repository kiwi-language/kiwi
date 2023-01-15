package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.flow.ExpressionValue;
import tech.metavm.flow.ReferenceValue;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.LongInstance;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionUtil;
import tech.metavm.object.instance.query.FieldExpression;
import tech.metavm.object.instance.query.ThisExpression;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

@EntityType("唯一约束项")
public class IndexField extends Entity {

    public static IndexField createFieldItem(Index constraint, Field field) {
        return new IndexField(
                constraint,
                field.getName(),
                new ReferenceValue(ExpressionUtil.fieldExpr(field))
        );
    }

    @EntityField("唯一约束")
    private final Index constraint;
    @EntityField("名称")
    private String name;
    @ChildEntity("值")
    private Value value;

    public IndexField(Index constraint, String name, Value value) {
        setName(name);
        this.constraint = constraint;
        setValue(value);// ;ValueFactory.getValue(valueDTO, new TypeParsingContext(constraint.getDeclaringType())));
        constraint.addField(this);
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

    public Field getField() {
        Expression expression = null;
        if(value instanceof ReferenceValue referenceValue) {
            expression = referenceValue.getExpression();
        }
        if(value instanceof ExpressionValue expressionValue) {
            expression = expressionValue.getExpression();
        }
        if(expression != null) {
            return extractField(expression);
        }
        return null;
    }

    private Field extractField(Expression expression) {
        if(expression instanceof FieldExpression fieldExpression) {
            if((fieldExpression.getInstance() instanceof ThisExpression) &&
                    fieldExpression.getFieldPath().size() == 1) {
                return fieldExpression.getFieldPath().get(0);
            }
        }
        return null;
    }

    public UniqueConstraintItemDTO toDTO(boolean forPersistence) {
        return new UniqueConstraintItemDTO(
                id,
                name,
                value.toDTO(forPersistence)
        );
    }

    public void setKeyItem(IndexKeyPO key, Instance fieldValue) {
        if(isColumnX()) {
            if(fieldValue.isNull()) {
                key.setColumnXPresent(false);
                key.setColumnX(0L);
            }
            else {
                key.setColumnXPresent(true);
                key.setColumnX(((LongInstance) fieldValue).getValue());
            }
        }
        else {
            int index = constraint.getItemIndex(this);
            Object indexValue;
            if(fieldValue.isNull()) {
                indexValue = null;
            }
            else if(fieldValue instanceof PrimitiveInstance primitiveInstance) {
                indexValue = primitiveInstance.getValue();
            }
            else {
                indexValue = NncUtils.requireNonNull(fieldValue.getId());
            }
            key.setColumn(index, IndexKeyPO.getIndexColumn(indexValue));
        }
    }

    public boolean isItemNull(IndexKeyPO key) {
        if (isColumnX()) {
            return key.isColumnXPresent();
        }
        else {
            return key.getColumn(constraint.getItemIndex(this)).equals(IndexKeyPO.NULL);
        }
    }

    public boolean isColumnX() {
        return value.getType().isLong() && constraint.isLastItem(this);
    }

    public Instance convertModelToInstance(Object model, IEntityContext entityContext) {
        if(model == null) {
            return InstanceUtils.nullInstance();
        }
        if(value.getType().isReference()) {
            if(!entityContext.containsModel(model)) {
                throw new InternalException("Model " + model + " does not exist in the context");
            }
            return entityContext.getInstance(model);
        }
        return InstanceUtils.resolveValue(value.getType(), model);
    }

}
