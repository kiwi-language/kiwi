package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;

import java.util.List;
import java.util.Objects;

import static tech.metavm.autograph.TranspileUtil.createType;

public class ListSizeResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(MethodSignature.create(createType(List.class), "size"));

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var qualifier = Objects.requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression());
        var array = expressionResolver.resolve(qualifier);
        return new FunctionExpression(Func.LEN, List.of(array));
    }
}
