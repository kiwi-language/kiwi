package tech.metavm.expression;

import tech.metavm.object.instance.Instance;

import java.util.List;

public interface ParsingContext {

    Expression parse(List<Var> varPath);

    Instance getInstance(long id);

    boolean isContextVar(Var var);

    Expression resolveVar(Var var);

    Expression getDefaultExpr();

}
