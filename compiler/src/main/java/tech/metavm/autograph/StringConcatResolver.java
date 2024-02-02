package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

import java.util.List;
import java.util.Objects;

import static tech.metavm.autograph.TranspileUtil.createType;

public class StringConcatResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(MethodSignature.create(createType(String.class), "concat", createType(String.class)));

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var qualifier = expressionResolver.resolve(Objects.requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression()));
        var argument = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        return new FunctionExpression(Func.CONCAT, List.of(qualifier, argument));
    }
}
