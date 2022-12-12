package tech.metavm.object.instance.query;

import tech.metavm.entity.Entity;
import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;

@ValueType("字段表达式")
public class FieldExpression extends Expression {

    private final Expression instance;
    private final Table<Field> fieldPath;

    public FieldExpression(Expression instance, Field field) {
        this(instance, List.of(field));
    }

    public FieldExpression(Expression instance, ClassType type, List<Long> fieldPath) {
//        super(type.getContext().getInstanceContext());
        this.instance = instance;
        ClassType tmp = type;
        List<Field> fields = new ArrayList<>();
        for (Long fieldId : fieldPath) {
            Field field = tmp.getField(fieldId);
            fields.add(field);
            tmp = (ClassType) field.getType();
        }
        this.fieldPath = new Table<>(Field.class, fields);
    }

    public FieldExpression(Expression instance, List<Field> fieldPath) {
//        super(instance.context);
        this.instance = instance;
        this.fieldPath = new Table<>(Field.class, fieldPath);
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
    public String buildSelf(VarType symbolType) {
        String fieldsExpr = switch (symbolType) {
            case ID -> NncUtils.join(fieldPath, f -> idVarName(f.getId()), ".");
            case NAME -> NncUtils.join(fieldPath, Field::getName, ".");
        };
        if(instance instanceof ThisExpression) {
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
        StringBuilder builder = new StringBuilder();
        for (Field field : fieldPath) {
            if(builder.length() > 0) {
                builder.append('.');
            }
            builder.append(field.getName());
        }
        return builder.toString();
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
}
