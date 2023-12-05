package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.expression.ThisExpression;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.rest.dto.IndexFieldDTO;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;

@EntityType("唯一约束项")
public class IndexField extends Entity {

    public static IndexField createFieldItem(Index constraint, Field field) {
        return new IndexField(
                constraint,
                field.getName(),
                field.getCode(),
                Value.reference(ExpressionUtil.propertyExpr(field))
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
        this.value = addChild(value, "value");
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

    public IndexFieldDTO toDTO(boolean forPersistence) {
        return new IndexFieldDTO(
                id,
                name,
                code,
                value.toDTO(forPersistence)
        );
    }

    public int getFieldIndex() {
        return index.getFieldIndex(this);
    }

    public Instance convertModelToInstance(Object model, IEntityContext context) {
        if (InstanceUtils.isPrimitive(model) || context.containsModel(model))
            return context.getInstance(model);
        else
            throw new InternalException("Model " + model + " does not exist in the context");

    }

}
