package tech.metavm.expression;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.ArrayTypeProvider;
import tech.metavm.object.type.IndexedTypeProvider;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.UnionTypeProvider;
import tech.metavm.util.InternalException;

public class SubParsingContext implements ParsingContext {
    private final ParsingContext parent;
    private final AllMatchExpression allMatchExpression;

    public SubParsingContext(AllMatchExpression allMatchExpression, ParsingContext parent) {
        this.allMatchExpression = allMatchExpression;
        this.parent = parent;
    }

    @Override
    public Instance getInstance(long id) {
        return parent.getInstance(id);
    }

    private boolean isSelfVar(Var var) {
        return allMatchExpression.getCursorAlias() != null && var.isName()
                && allMatchExpression.getCursorAlias().equals(var.getName());
    }

    @Override
    public boolean isContextVar(Var var) {
        return isSelfVar(var) || this.parent != null && this.parent.isContextVar(var);
    }

    @Override
    public Expression resolveVar(Var var) {
        if (isSelfVar(var)) {
            return allMatchExpression.createCursor();
        }
        else if(parent != null && parent.isContextVar(var)) {
            return parent.resolveVar(var);
        }
        throw new InternalException(var + " is not a context var of " + this);
    }

    @Override
    public Expression getDefaultExpr() {
        return allMatchExpression.createCursor();
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
    public IndexedTypeProvider getTypeProvider() {
        return parent.getTypeProvider();
    }

    @Override
    public ArrayTypeProvider getArrayTypeProvider() {
        return parent.getArrayTypeProvider();
    }

    @Override
    public UnionTypeProvider getUnionTypeProvider() {
        return parent.getUnionTypeProvider();
    }
}
