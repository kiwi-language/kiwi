package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;
import org.metavm.api.lang.PasswordUtils;

import java.util.List;

public class RandomPasswordResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(PasswordUtils.class),
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
