package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.api.builtin.Password;
import org.metavm.expression.Expression;
import org.metavm.expression.Func;
import org.metavm.expression.FunctionExpression;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class NewPasswordResolver implements NewResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(Password.class), "Password",
                             TranspileUtil.createClassType(String.class))
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiNewExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var password = expressionResolver.resolve(requireNonNull(methodCallExpression.getArgumentList()).getExpressions()[0]);
        return new FunctionExpression(Func.PASSWORD, password);
    }
}
