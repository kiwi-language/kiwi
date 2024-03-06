package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.type.*;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeParsingContext extends BaseParsingContext {

    public static TypeParsingContext create(ClassType type, IEntityContext context) {
        return new TypeParsingContext(
                context.getInstanceContext(),
                new ContextTypeRepository(context),
                new ContextArrayTypeProvider(context),
                context.getUnionTypeContext(),
                type
        );
    }

    private final ClassType type;
    private final ThisExpression thisExpression;
    private final java.util.function.Function<Long, Instance> getInstanceFunc;

    public TypeParsingContext(InstanceProvider instanceProvider,
                              IndexedTypeProvider typeProvider,
                              ArrayTypeProvider arrayTypeProvider,
                              UnionTypeProvider unionTypeProvider,
                              ClassType type) {
        super(instanceProvider, typeProvider, arrayTypeProvider, unionTypeProvider);
        this.type = type;
        thisExpression = new ThisExpression(type);
        this.getInstanceFunc = id -> instanceProvider.get(new PhysicalId(id));
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
    public Type getExpressionType(Expression expression) {
        return expression.getType();
    }

    @Override
    public Expression getDefaultExpr() {
        return thisExpression;
    }


    public static List<Field> getFields(ClassType type, List<Var> varPath) {
        return getFields(type, varPath, true);
    }

    public static List<Field> getFields(ClassType type, List<Var> varPath, boolean errorWhenNotFound) {
        List<Field> fields = new ArrayList<>();
        ClassType t = type;
        Field field;
        Iterator<Var> varIt = varPath.iterator();
        while (varIt.hasNext()) {
            Var var = varIt.next();
            field = getField(t, var);
            if (field == null) {
                if (errorWhenNotFound) {
                    throw BusinessException.invalidExpression("属性'" + var + "'不存在");
                } else {
                    return null;
                }
            }
            fields.add(field);
            if (varIt.hasNext()) {
                t = (ClassType) field.getType().getConcreteType();
            }
        }
        return fields;
    }

    public static Field getField(ClassType type, Var var) {
        return switch (var.getType()) {
            case NAME -> type.tryGetFieldByName(var.getName());
            case ID -> type.getField(var.getId());
        };
    }

}
