package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.List;

public class ToStringResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
        MethodSignature.create(
                TranspileUtil.createClassType(Object.class),
                "toString"
        )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.TO_STRING,
                expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()));
    }
}
