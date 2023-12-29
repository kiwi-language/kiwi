package tech.metavm.expression;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.IndexedTypeProvider;
import tech.metavm.object.type.Type;

public interface ParsingContext {

    Instance getInstance(long id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

    Type getExpressionType(Expression expression);

    InstanceProvider getInstanceProvider();

    IndexedTypeProvider getTypeProvider();

    ArrayTypeProvider getArrayTypeProvider();

}
