package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.api.builtin.Password;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.Value;
import org.metavm.flow.Values;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class NewPasswordResolver implements NewResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtils.createClassType(Password.class), "Password",
                             TranspileUtils.createClassType(String.class))
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Value resolve(PsiNewExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var password = expressionResolver.resolve(requireNonNull(methodCallExpression.getArgumentList()).getExpressions()[0]);
        return Values.node(methodGenerator.createFunctionCall(StdFunction.password.get(), List.of(password)));
    }
}
