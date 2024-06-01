package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

import java.util.List;
import java.util.Objects;

public class ObjectsToStringResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.createStatic(TranspileUtil.createClassType(Objects.class), "toString",
                            TranspileUtil.createClassType(Object.class)
                    )
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var operand = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        return new FunctionExpression(Func.TO_STRING, operand);
    }
}
