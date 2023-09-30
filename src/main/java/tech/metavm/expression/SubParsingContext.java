package tech.metavm.expression;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.InternalException;

public class SubParsingContext implements ParsingContext {
    private final ParsingContext parent;
    private final CursorExpression cursor;

    public SubParsingContext(CursorExpression cursor, ParsingContext parent) {
        this.cursor = cursor;
        this.parent = parent;
    }


    @Override
    public Instance getInstance(long id) {
        return parent.getInstance(id);
    }

    private boolean isSelfVar(Var var) {
        return cursor.getAlias() != null && var.isName() && cursor.getAlias().equals(var.getName());
    }

    @Override
    public boolean isContextVar(Var var) {
        return isSelfVar(var) || this.parent != null && this.parent.isContextVar(var);
    }

    @Override
    public Expression resolveVar(Var var) {
        if (isSelfVar(var)) {
            return cursor;
        }
        else if(parent != null && parent.isContextVar(var)) {
            return parent.resolveVar(var);
        }
        throw new InternalException(var + " is not a context var of " + this);
    }

    @Override
    public Expression getDefaultExpr() {
        return cursor;
    }

    @Nullable
    @Override
    public IInstanceContext getInstanceContext() {
        return parent.getInstanceContext();
    }


}
