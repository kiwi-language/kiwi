package org.metavm.autograph;

import com.intellij.psi.PsiNewExpression;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.Node;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class NewDateResolver implements NewResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtils.createClassType(Date.class), "Date"),
                    MethodSignature.create(TranspileUtils.createClassType(Date.class), "Date",
                            TranspileUtils.createPrimitiveType(long.class))
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Node resolve(PsiNewExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var args = requireNonNull(methodCallExpression.getArgumentList()).getExpressions();
        if(args.length == 0)
            return methodGenerator.createFunctionCall(StdFunction.now.get().getRef());
        else {
            expressionResolver.resolve(args[0]);
            return methodGenerator.createFunctionCall(StdFunction.time.get().getRef());
        }
    }
}
