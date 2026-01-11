package org.manul.expression;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.InstanceProvider;
import org.manul.object.instance.core.Value;
import org.manul.object.type.IndexedTypeDefProvider;
import org.manul.object.type.Type;

public interface ParsingContext {

    Value getInstance(Id id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

    Type getExpressionType(Expression expression);

    InstanceProvider getInstanceProvider();

    IndexedTypeDefProvider getTypeDefProvider();

}
