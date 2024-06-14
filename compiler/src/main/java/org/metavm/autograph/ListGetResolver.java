package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.NodeExpression;

import java.util.List;

public class ListGetResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES =
            List.of(MethodSignature.create(TranspileUtil.createClassType(List.class), "get",
                    TranspileUtil.createPrimitiveType(int.class)));

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new NodeExpression(
                methodGenerator.createGetElement(
                        expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()),
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0])
                )
        );
    }
}
