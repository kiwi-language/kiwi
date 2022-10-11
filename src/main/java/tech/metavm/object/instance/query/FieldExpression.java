package tech.metavm.object.instance.query;

import tech.metavm.entity.Entity;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldExpression extends Expression {

    private final Expression instance;
    private final List<Field> fieldPath;

    public FieldExpression(Expression instance, Field field) {
        this(instance, List.of(field));
    }

    public FieldExpression(Expression instance, Type type, List<Long> fieldPath) {
        this.instance = instance;
        Type tmp = type;
        List<Field> fields = new ArrayList<>();
        for (Long fieldId : fieldPath) {
            Field field = tmp.getField(fieldId);
            fields.add(field);
            tmp = field.getType();
        }
        this.fieldPath = Collections.unmodifiableList(fields);
    }

    public FieldExpression(Expression instance, List<Field> fieldPath) {
        this.instance = instance;
        this.fieldPath = fieldPath;
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
        String instanceExpr = instance.build(symbolType, instance.precedence() > precedence());
        return switch (symbolType) {
            case ID -> instanceExpr + "." + NncUtils.join(fieldPath, f -> idVarName(f.getId()), ".");
            case NAME -> instanceExpr + "." + NncUtils.join(fieldPath, Field::getName, ".");
        };
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
