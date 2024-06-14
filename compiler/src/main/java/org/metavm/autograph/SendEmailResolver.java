package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.expression.Expression;
import org.metavm.lang.EmailUtils;

import java.util.List;

public class SendEmailResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(EmailUtils.class),
                    "send",
                    TranspileUtil.createClassType(String.class),
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
        var recipient = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        var subject = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[1]);
        var content = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[2]);
        methodGenerator.createFunctionCall(
                NativeFunctions.getSendEmail(),
                List.of(recipient, subject, content)
        );
        return null;
    }
}
