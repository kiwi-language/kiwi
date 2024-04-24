package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.*;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeParsingContext extends BaseParsingContext {

    public static TypeParsingContext create(Klass type, IEntityContext context) {
        return new TypeParsingContext(
                context.getInstanceContext(),
                new ContextTypeDefRepository(context),
                new ContextArrayTypeProvider(context),
                context.getUnionTypeContext(),
                type
        );
    }

    private final Klass klass;
    private final ThisExpression thisExpression;
    private final java.util.function.Function<Id, Instance> getInstanceFunc;

    public TypeParsingContext(InstanceProvider instanceProvider,
                              IndexedTypeDefProvider typeProvider,
                              ArrayTypeProvider arrayTypeProvider,
                              UnionTypeProvider unionTypeProvider,
                              Klass klass) {
        super(instanceProvider, typeProvider, arrayTypeProvider, unionTypeProvider);
        this.klass = klass;
        thisExpression = new ThisExpression(klass.getType());
        this.getInstanceFunc = instanceProvider::get;
    }

    public Klass getKlass() {
        return klass;
    }

    @Override
    public Instance getInstance(Id id) {
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


    public static List<Field> getFields(Klass type, List<Var> varPath) {
        return getFields(type, varPath, true);
    }

    public static List<Field> getFields(Klass type, List<Var> varPath, boolean errorWhenNotFound) {
        List<Field> fields = new ArrayList<>();
        Klass t = type;
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
                t = ((ClassType) field.getType().getConcreteType()).resolve();
            }
        }
        return fields;
    }

    public static Field getField(Klass type, Var var) {
        return switch (var.getType()) {
            case NAME -> type.tryGetFieldByName(var.getName());
            case ID -> type.getField(var.getId());
        };
    }

}
