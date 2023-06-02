package tech.metavm.expression;

import tech.metavm.entity.*;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;

@EntityType("字段表达式")
public class FieldExpression extends Expression {

    @ChildEntity("对象")
    private final Expression instance;

    @ChildEntity("字段路径")
    private final Table<Field> fieldPath = new Table<>(Field.class);

    public FieldExpression(Expression instance, Field field) {
        this(instance, List.of(field));
    }

    public FieldExpression(Expression instance, ClassType type, List<Long> fieldPath) {
        this.instance = instance;
        ClassType tmp = type;
        List<Field> fields = new ArrayList<>();
        for (Long fieldId : fieldPath) {
            Field field = tmp.getField(fieldId);
            fields.add(field);
            tmp = (ClassType) field.getType();
        }
        this.fieldPath.addAll(fields);
    }

    public FieldExpression(Expression instance, List<Field> fieldPath) {
        this.instance = instance;
        this.fieldPath.addAll(fieldPath);
    }

    public Field getLastField() {
        return fieldPath.get(fieldPath.size() - 1);
    }

    public List<Field> getFieldPath() {
        return fieldPath;
    }

    public List<Long> getFieldIds() {
        return NncUtils.map(fieldPath, Entity::getId);
    }

    public List<String> getFieldIdPath() {
        return NncUtils.map(fieldPath, field -> idVarName(field.getId()));
    }

    @Override
    public Type getType() {
        return getLastField().getType();
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new FieldExpression(instance, fieldPath);
    }

    @Override
    public String buildSelf(VarType symbolType) {
        String fieldsExpr = switch (symbolType) {
            case ID -> NncUtils.join(fieldPath, f -> idVarName(f.getId()), ".");
            case NAME -> NncUtils.join(fieldPath, Field::getName, ".");
        };
        if((instance instanceof CursorExpression cursorExpression) && cursorExpression.getAlias() == null) {
            return fieldsExpr;
        }
        else {
            String instanceExpr = instance.build(symbolType, instance.precedence() > precedence());
            return instanceExpr + "." + fieldsExpr;
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    public String getPathString() {
        return NncUtils.join(fieldPath, Field::getName, ".");
    }

    public Expression getInstance() {
        return instance;
    }

    public FieldExpression simply() {
        if(instance instanceof FieldExpression instanceExpr) {
            instanceExpr = instanceExpr.simply();
            List<Field> fields = new ArrayList<>(instanceExpr.getFieldPath());
            fields.addAll(this.fieldPath);
            return new FieldExpression(instanceExpr.getInstance(), fields);
        }
        else {
            return this;
        }
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return super.extractExpressionsRecursively(klass);
    }
}
