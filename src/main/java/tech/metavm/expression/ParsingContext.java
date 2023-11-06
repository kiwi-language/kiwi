package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public interface ParsingContext {

    Instance getInstance(long id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

    Type getExpressionType(Expression expression);

    @Nullable
    IInstanceContext getInstanceContext();

    @Nullable
    default IEntityContext getEntityContext() {
        return NncUtils.get(getInstanceContext(), IInstanceContext::getEntityContext);
    }

}
