package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

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
