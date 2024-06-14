package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.lang.SessionUtils;

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
