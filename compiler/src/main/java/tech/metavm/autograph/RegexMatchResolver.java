package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;
import tech.metavm.lang.RegexUtils;

import java.util.List;

public class RegexMatchResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(RegexUtils.class),
                    "match",
                    TranspileUtil.createClassType(String.class),
                    TranspileUtil.createClassType(String.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var pattern = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        var str = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[1]);
        return new FunctionExpression(Func.REGEX_MATCH, List.of(pattern, str));
    }
}
