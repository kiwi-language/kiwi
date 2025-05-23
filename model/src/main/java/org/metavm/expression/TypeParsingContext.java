package org.metavm.expression;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeParsingContext extends BaseParsingContext {

    public static TypeParsingContext create(Klass type, IInstanceContext context) {
        return new TypeParsingContext(
                context,
                new ContextTypeDefRepository(context),
                type
        );
    }

    private final Klass klass;
    private final ThisExpression thisExpression;
    private final java.util.function.Function<Id, Value> getInstanceFunc;

    public TypeParsingContext(InstanceProvider instanceProvider,
                              IndexedTypeDefProvider typeProvider,
                              Klass klass) {
        super(instanceProvider, typeProvider);
        this.klass = klass;
        thisExpression = new ThisExpression(klass.getType());
        this.getInstanceFunc = id -> instanceProvider.get(id).getReference();
    }

    public Klass getKlass() {
        return klass;
    }

    @Override
    public Value getInstance(Id id) {
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
                    throw BusinessException.invalidExpression("Property '" + var + "' does not exist");
                } else {
                    return null;
                }
            }
            fields.add(field);
            if (varIt.hasNext()) {
                t = ((ClassType) field.getType().getConcreteType()).getKlass();
            }
        }
        return fields;
    }

    public static Field getField(Klass type, Var var) {
        return switch (var.getType()) {
            case NAME -> type.findFieldByName(var.getName());
            case ID -> type.getField(var.getId());
        };
    }

}
