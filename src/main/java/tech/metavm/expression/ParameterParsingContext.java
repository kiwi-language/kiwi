package tech.metavm.expression;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.InternalException;

public class ParameterParsingContext implements ParsingContext {

    private final Flow flow;
    private final InstanceContext instanceContext;

    public ParameterParsingContext(Flow flow, InstanceContext instanceContext) {
        this.flow = flow;
        this.instanceContext = instanceContext;
    }

    @Override
    public Instance getInstance(long id) {
        return instanceContext.get(id);
    }

    @Override
    public boolean isContextVar(Var var) {
        if(var.isId()) {
            return var.getId() == 0L || var.getId() == 1L;
        }
        else {
            return false;
        }
    }

    @Override
    public Expression resolveVar(Var var) {
        if(isContextVar(var)) {
            return null;
        }
        else {
            throw new InternalException("'" + var + "' is not a context variable");
        }
    }

    @Override
    public Expression getDefaultExpr() {
        return null;
    }

    @Override
    public Type getExpressionType(Expression expression) {
        return null;
    }

    @Nullable
    @Override
    public IInstanceContext getInstanceContext() {
        return null;
    }

}
