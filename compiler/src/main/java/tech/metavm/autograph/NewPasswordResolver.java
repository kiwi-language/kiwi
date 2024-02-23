package tech.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import tech.metavm.builtin.Password;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

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
