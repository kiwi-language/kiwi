package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.ExpressionValue;
import tech.metavm.flow.ReferenceValue;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.ExpressionUtil;
import tech.metavm.object.instance.query.FieldExpression;
import tech.metavm.object.instance.query.ThisExpression;

@EntityType("唯一约束项")
public class IndexConstraintItem extends Entity {

    public static IndexConstraintItem createFieldItem(IndexConstraintRT constraint, Field field) {
        return new IndexConstraintItem(
                constraint,
                field.getName(),
                new ReferenceValue(ExpressionUtil.fieldExpr(field))
        );
    }

    @EntityField("唯一约束")
    private final transient IndexConstraintRT constraint;
    @EntityField("名称")
    private String name;
    @EntityField("值")
    private Value value;

    public IndexConstraintItem(IndexConstraintRT constraint, String name, Value value) {
        setName(name);
        this.constraint = constraint;
        setValue(value);// ;ValueFactory.getValue(valueDTO, new TypeParsingContext(constraint.getDeclaringType())));
        constraint.addItem(this);
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

}
