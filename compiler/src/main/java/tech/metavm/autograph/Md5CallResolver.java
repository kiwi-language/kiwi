package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;
import tech.metavm.lang.MD5Utils;

import java.util.List;

public class Md5CallResolver implements MethodCallResolver {

    public static final List<MethodSignature> SIGNATURES = List.of(
            MethodSignature.createStatic(
                    TranspileUtil.createClassType(MD5Utils.class),
                    "md5",
                    TranspileUtil.createClassType(String.class)
            )
    );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        return new FunctionExpression(Func.MD5, expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]));
    }
}
