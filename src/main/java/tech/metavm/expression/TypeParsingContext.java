package tech.metavm.expression;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeParsingContext implements ParsingContext {

    private final ClassType type;
    private final ThisExpression thisExpression;
    private final java.util.function.Function<Long, Instance> getInstanceFunc;
    private final IInstanceContext instanceContext;

    public TypeParsingContext(ClassType classType) {
        this(classType, id -> {
            throw new UnsupportedOperationException();
        });
    }

    public TypeParsingContext(ClassType type, IInstanceContext instanceContext) {
        this.type = type;
        thisExpression = new ThisExpression(type);
        this.getInstanceFunc = instanceContext::get;
        this.instanceContext = instanceContext;
    }

    public TypeParsingContext(ClassType type, java.util.function.Function<Long, Instance> getInstanceFunc) {
        this.type = type;
        this.getInstanceFunc = getInstanceFunc;
        thisExpression = new ThisExpression(type);
        instanceContext = null;
    }

    public ClassType getType() {
        return type;
    }

    @Override
    public Instance getInstance(long id) {
        return getInstanceFunc.apply(id);
    }

    @Override
    public boolean isContextVar(Var var) {
        return var.isName() && var.getName().equals("this");
    }

    @Override
    public Expression resolveVar(Var var) {
        if (isContextVar(var)) {
            return thisExpression;
        }
        throw new InternalException(var + " is not the context var of " + this);
    }

    @Override
    public Expression getDefaultExpr() {
        return thisExpression;
    }

    @Override
    @Nullable
    public IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    public static List<Field> getFields(ClassType type, List<Var> varPath) {
        return getFields(type, varPath, true);
    }

    public static List<Field> getFields(ClassType type, List<Var> varPath, boolean errorWhenNotFound) {
        List<Field> fields = new ArrayList<>();
        ClassType t = type;
        Field field;
        Iterator<Var> varIt = varPath.iterator();
        while (varIt.hasNext()){
            Var var = varIt.next();
            field = getField(t, var);
            if(field == null) {
                if(errorWhenNotFound) {
                    throw BusinessException.invalidExpression("属性'" + var + "'不存在");
                }
                else {
                    return null;
                }
            }
            fields.add(field);
            if(varIt.hasNext()) {
                t = (ClassType) field.getType().getConcreteType();
            }
        }
        return fields;
    }

    public static Field getField(ClassType type, Var var) {
        return switch (var.getType()) {
            case NAME -> type.getFieldByName(var.getName());
            case ID -> type.getField(var.getId());
        };
    }

}
