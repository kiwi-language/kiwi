package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.api.builtin.Password;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.Node;

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
    public Node resolve(PsiNewExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        expressionResolver.resolve(requireNonNull(methodCallExpression.getArgumentList()).getExpressions()[0]);
        return methodGenerator.createInvokeFunction(StdFunction.password.get().getRef());
    }
}
