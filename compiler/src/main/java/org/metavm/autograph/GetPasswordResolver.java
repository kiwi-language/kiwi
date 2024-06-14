package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.builtin.Password;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.List;

public class GetPasswordResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(Password.class), "getPassword")
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression,
                              ExpressionResolver expressionResolver,
                              MethodGenerator methodGenerator) {
        var password = expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression());
        return new FunctionExpression(Func.GET_PASSWORD, password);
    }
}
