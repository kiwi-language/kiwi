package tech.metavm.expression;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;

import javax.annotation.Nullable;
import java.util.List;

public interface ParsingContext {

    Expression parse(List<Var> varPath);

    Instance getInstance(long id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

    @Nullable
    IInstanceContext getInstanceContext();

}
