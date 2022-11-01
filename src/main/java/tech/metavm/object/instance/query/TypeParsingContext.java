package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;

import java.util.ArrayList;
import java.util.List;

public class TypeParsingContext implements ParsingContext {

    private final Type type;

    public TypeParsingContext(Type type) {
        this.type = type;
    }

    @Override
    public FieldExpression parse(List<Var> varPath) {
        return new FieldExpression(ExpressionUtil.thisObject(type), getFields(type, varPath));
    }

    public static List<Field> getFields(Type type, List<Var> varPath) {
        List<Field> fields = new ArrayList<>();
        Type t = type;
        Field field;
        for (Var var : varPath) {
            field = getField(t, var);
            if(field == null) {
                throw BusinessException.invalidExpression("属性'" + var + "'不存在");
            }
            fields.add(field);
            t = field.getType();
        }
        return fields;
    }

    public static Field getField(Type type, Var var) {
        return switch (var.getType()) {
            case NAME -> type.getFieldByName(var.getStringSymbol());
            case ID -> type.getField(var.getLongSymbol());
        };
    }

    @Override
    public EntityContext getEntityContext() {
        return type.getContext();
    }
}
