package tech.metavm.expression;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.IndexedTypeDefProvider;
import tech.metavm.object.type.Type;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;

public class SubParsingContext implements ParsingContext {
    private final ParsingContext parent;
    private final Type elementType;
    private final @Nullable String alias;

    public SubParsingContext(@Nullable String alias, Type elementType, ParsingContext parent) {
        this.alias = alias;
        this.elementType = elementType;
        this.parent = parent;
    }

    @Override
    public Instance getInstance(Id id) {
        return parent.getInstance(id);
    }

    private boolean isSelfVar(Var var) {
        return alias != null && var.isName() && alias.equals(var.getName());
    }

    @Override
    public boolean isContextVar(Var var) {
        return isSelfVar(var) || this.parent != null && this.parent.isContextVar(var);
    }

    @Override
    public Expression resolveVar(Var var) {
        if (isSelfVar(var)) {
            return new CursorExpression(elementType, alias);
        }
        else if(parent != null && parent.isContextVar(var)) {
            return parent.resolveVar(var);
        }
        throw new InternalException(var + " is not a context var of " + this);
    }

    @Override
    public Expression getDefaultExpr() {
        return new CursorExpression(elementType, alias);
    }

    @Override
    public Type getExpressionType(Expression expression) {
        return parent.getExpressionType(expression);
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return parent.getInstanceProvider();
    }

    @Override
    public IndexedTypeDefProvider getTypeDefProvider() {
        return parent.getTypeDefProvider();
    }

}
