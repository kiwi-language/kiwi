package tech.metavm.object.instance.query;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListExpression extends Expression {

    private final List<Expression> expressions;

    public ListExpression(List<Expression> expressions) {
        this.expressions = Collections.unmodifiableList(expressions);
    }

    public static ListExpression merge(Expression first, Expression second) {
        if(first instanceof ListExpression listExpression) {
            List<Expression> rest = listExpression.expressions;
            List<Expression> expressions = new ArrayList<>(rest.size() + 1);
            expressions.addAll(rest);
            expressions.add(second);
            return new ListExpression(expressions);
        }
        else {
            return new ListExpression(List.of(first, second));
        }
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String toString() {
        return "(" + NncUtils.join(expressions, Objects::toString, ", ") + ")";
    }

}
