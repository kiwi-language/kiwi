package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

public class SubParsingContext implements ParsingContext {
    private final ParsingContext parent;
    private final CursorExpression cursor;

    public SubParsingContext(CursorExpression cursor, ParsingContext parent) {
        this.cursor = cursor;
        this.parent = parent;
    }

    @Override
    public Expression parse(List<Var> vars) {
        NncUtils.requireMinimumSize(vars, 1);
        Var firstVar = vars.get(0);
        if (isSelfVar(firstVar)) {
            NncUtils.requireMinimumSize(vars, 2);
            return createFieldExpression(vars.subList(1, vars.size()));
        } else if (parent != null && parent.isContextVar(firstVar)) {
            return parent.parse(vars);
        }
        return createFieldExpression(vars);
    }

    private FieldExpression createFieldExpression(List<Var> vars) {
        List<Field> fields = TypeParsingContext.getFields(cursor.getType(), vars, false);
        return new FieldExpression(cursor, fields);
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


}
