package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.flow.Function;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class NativeFunctionCallResolver implements MethodCallResolver {

    private final MethodSignature signature;
    private final Function function;

    public NativeFunctionCallResolver(MethodSignature signature, Function function) {
        this.signature = signature;
        this.function = function;
    }

    @Override
    public List<MethodSignature> getSignatures() {
        return List.of(signature);
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var arguments = new ArrayList<Expression>();
        if(!signature.isStatic())
            arguments.add(expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression()));
        for (PsiExpression expression : methodCallExpression.getArgumentList().getExpressions()) {
            arguments.add(expressionResolver.resolve(expression));
        }
        var node = methodGenerator.createFunctionCall(function, arguments);
        return function.getReturnType().isVoid() ? null : Expressions.node(node);
    }

}
