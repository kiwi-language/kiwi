package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;
import org.metavm.api.lang.UUIDUtils;

import java.util.List;

public class RandomUUIDResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(UUIDUtils.class),
                    "randomUUID"
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.UUID, List.of());
    }

}
