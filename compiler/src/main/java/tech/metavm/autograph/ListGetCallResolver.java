package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.NodeExpression;

import java.util.List;

public class ListGetCallResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES =
            List.of(MethodSignature.create(TranspileUtil.createType(List.class), "get",
                    TranspileUtil.createType(int.class)));

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
