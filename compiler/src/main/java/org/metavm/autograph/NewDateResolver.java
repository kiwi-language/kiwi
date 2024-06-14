package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class NewDateResolver implements NewResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(Date.class), "Date"),
                    MethodSignature.create(TranspileUtil.createClassType(Date.class), "Date",
                            TranspileUtil.createPrimitiveType(long.class))
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiNewExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var args = requireNonNull(methodCallExpression.getArgumentList()).getExpressions();
        if(args.length == 0)
            return new FunctionExpression(Func.NOW, List.of());
        else
            return new FunctionExpression(Func.TIME, List.of(expressionResolver.resolve(args[0])));
    }
}
