package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.Expression;
import tech.metavm.object.instance.query.TypeParsingContext;
import tech.metavm.object.instance.query.VarType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EntityType("唯一约束项")
public class UniqueConstraintItem extends Entity {

    public static final Pattern ID_EXPR_PTN = Pattern.compile("\\$([^.]+)");

    public static UniqueConstraintItem createFieldItem(UniqueConstraintRT constraint, Field field) {
        return new UniqueConstraintItem(
                constraint,
                field.getName(),
                new ValueDTO(
                        ValueKind.REFERENCE.code(),
                        field.getName(),
                        null
                )
        );
    }

    @EntityField("唯一约束")
    private final transient UniqueConstraintRT constraint;
    @EntityField("名称")
    private String name;
    @EntityField("值")
    private Value value;

    public UniqueConstraintItem(UniqueConstraintRT constraint, String name, ValueDTO valueDTO) {
//        super(constraint.getContext());
        setName(name);
        this.constraint = constraint;
        setValue(ValueFactory.getValue(valueDTO, new TypeParsingContext(constraint.getDeclaringType())));
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
            String expr = expression.buildSelf(VarType.NAME);
            Matcher matcher = ID_EXPR_PTN.matcher(expr);
            if(matcher.matches()) {
//                long id = Long.parseLong(matcher.group(1));
                String fieldName = matcher.group(1);
                return constraint.getDeclaringType().getFieldByName(fieldName);
            }
        }
        return null;
    }

    public UniqueConstraintItemDTO toDTO(boolean forPersistence) {
        return new UniqueConstraintItemDTO(
                name,
                value.toDTO(forPersistence)
        );
    }

}
