package tech.metavm.expression;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.*;

public interface ParsingContext {

    Instance getInstance(Id id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

    Type getExpressionType(Expression expression);

    InstanceProvider getInstanceProvider();

    IndexedTypeDefProvider getTypeDefProvider();

    ArrayTypeProvider getArrayTypeProvider();

    UnionTypeProvider getUnionTypeProvider();

}
