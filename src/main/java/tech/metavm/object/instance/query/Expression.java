package tech.metavm.object.instance.query;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.Type;

import java.util.ArrayList;
import java.util.List;

@EntityType("表达式")
public abstract class Expression extends Entity {

    public abstract String buildSelf(VarType symbolType);

    public abstract int precedence();

    public final String build(VarType symbolType, boolean withParenthesis) {
        String expr = buildSelf(symbolType);
        return withParenthesis ? "(" + expr + ")" : expr;
    }

    public abstract Type getType();

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + buildSelf(VarType.NAME);
    }

    public static String idVarName(long id) {
        return "$" + id;
    }

    public <T extends Expression> List<T> extractExpressions(Class<T> klass) {
        List<T> results = new ArrayList<>();
        if(klass.isInstance(this)) {
            results.add(klass.cast(this));
        }
        results.addAll(extractExpressionsRecursively(klass));
        return results;
    }

    protected  <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return List.of();
    }

}
