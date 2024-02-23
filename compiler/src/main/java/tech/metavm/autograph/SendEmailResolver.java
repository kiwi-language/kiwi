package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expression;
import tech.metavm.lang.EmailUtils;

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
