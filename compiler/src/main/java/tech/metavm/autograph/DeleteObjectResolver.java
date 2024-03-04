package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expression;
import tech.metavm.lang.ObjectUtils;

import java.util.List;

public class DeleteObjectResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
        MethodSignature.createStatic(
                TranspileUtil.createClassType(ObjectUtils.class),
                "delete",
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
                NativeFunctions.getDelete(),
                List.of(expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]))
        );
        return null;
    }
}
