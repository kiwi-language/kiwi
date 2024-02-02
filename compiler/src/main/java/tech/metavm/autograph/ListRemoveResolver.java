package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.NodeExpression;

import java.util.List;

public class ListRemoveResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.create(TranspileUtil.createType(List.class), "remove",
                    TranspileUtil.createType(Object.class))
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new NodeExpression(
                methodGenerator.createRemoveElement(
                        expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()),
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0])
                )
        );
    }
}
