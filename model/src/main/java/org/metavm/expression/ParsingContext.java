package org.metavm.expression;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.IndexedTypeDefProvider;
import org.metavm.object.type.Type;

public interface ParsingContext {

    Value getInstance(Id id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

    Type getExpressionType(Expression expression);

    InstanceProvider getInstanceProvider();

    IndexedTypeDefProvider getTypeDefProvider();

}
