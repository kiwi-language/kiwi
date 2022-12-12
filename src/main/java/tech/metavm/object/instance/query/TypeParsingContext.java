package tech.metavm.object.instance.query;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeParsingContext implements ParsingContext {

    private final ClassType type;

    public TypeParsingContext(ClassType type) {
        this.type = type;
    }

    @Override
    public FieldExpression parse(List<Var> varPath) {
        return new FieldExpression(ExpressionUtil.thisObject(type), getFields(type, varPath));
    }

    public static List<Field> getFields(ClassType type, List<Var> varPath) {
        List<Field> fields = new ArrayList<>();
        ClassType t = type;
        Field field;
        Iterator<Var> varIt = varPath.iterator();
        while (varIt.hasNext()){
            Var var = varIt.next();
            field = getField(t, var);
            if(field == null) {
                throw BusinessException.invalidExpression("属性'" + var + "'不存在");
            }
            fields.add(field);
            if(varIt.hasNext()) {
                t = (ClassType) field.getType();
            }
        }
        return fields;
    }

    public static Field getField(ClassType type, Var var) {
        return switch (var.getType()) {
            case NAME -> type.getFieldByName(var.getStringSymbol());
            case ID -> type.getField(var.getLongSymbol());
        };
    }

}
