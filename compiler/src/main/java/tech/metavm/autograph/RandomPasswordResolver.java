package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;
import tech.metavm.util.PasswordUtils;

import java.util.List;

public class RandomPasswordResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createType(PasswordUtils.class),
                    "randomPassword"
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.RANDOM_PASSWORD, List.of());
    }
}
