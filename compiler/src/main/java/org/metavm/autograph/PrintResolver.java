package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.expression.Expression;
import org.metavm.api.lang.SystemUtils;

import java.util.List;

public class PrintResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(SystemUtils.class),
                    "print",
                    TranspileUtil.createType(Object.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        methodGenerator.createFunctionCall(
                NativeFunctions.getPrint(),
                List.of(expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]))
        );
        return null;
    }
}
