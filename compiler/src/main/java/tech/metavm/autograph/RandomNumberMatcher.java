package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;
import tech.metavm.lang.NumberUtils;

import java.util.List;

public class RandomNumberMatcher implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(NumberUtils.class),
                    "random",
                    TranspileUtil.createPrimitiveType(long.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.BOUNDED_RANDOM, List.of(
                expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0])
        ));
    }
}
