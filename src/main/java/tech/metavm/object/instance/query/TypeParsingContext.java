package tech.metavm.object.instance.query;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.InstanceContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeParsingContext implements ParsingContext {

    private final ClassType type;
    private final IInstanceContext instanceContext;

    public TypeParsingContext(ClassType type, IInstanceContext instanceContext) {
        this.type = type;
        this.instanceContext = instanceContext;
    }

    @Override
    public FieldExpression parse(List<Var> varPath) {
        return new FieldExpression(ExpressionUtil.thisObject(type), getFields(type, varPath));
    }

    public ClassType getType() {
        return type;
    }

    @Override
    public IInstanceContext getInstanceContext() {
        return instanceContext;
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
