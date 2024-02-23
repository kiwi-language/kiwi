package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Expressions;
import tech.metavm.lang.SessionUtils;

import java.util.List;

public class GetSessionEntryResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(SessionUtils.class),
                    "getEntry",
                    TranspileUtil.createClassType(String.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return Expressions.node(
                methodGenerator.createFunctionCall(
                        NativeFunctions.getGetSessionEntry(),
                        List.of(
                                expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0])
                        )
                )
        );
    }
}
