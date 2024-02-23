package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expression;
import tech.metavm.lang.SessionUtils;

import java.util.List;

public class SetSessionEntryResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(SessionUtils.class),
                    "setEntry",
                    TranspileUtil.createClassType(String.class),
                    TranspileUtil.createClassType(Object.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        methodGenerator.createFunctionCall(
                NativeFunctions.getSetSessionEntry(),
                List.of(
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]),
                        expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[1]
                        )
                )
        );
        return null;
    }
}
