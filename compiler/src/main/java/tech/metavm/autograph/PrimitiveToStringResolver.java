package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPrimitiveType;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionExpression;
import tech.metavm.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrimitiveToStringResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES;

    static {
        var signatures = new ArrayList<MethodSignature>();
        for (PsiPrimitiveType primitiveType : TranspileUtil.getPrimitiveTypes()) {
            signatures.add(
                    MethodSignature.createStatic(
                            TranspileUtil.createType(ReflectionUtils.classForName(primitiveType.getBoxedTypeName())),
                            "toString",
                            primitiveType
                    )
            );
        }
        SIGNATURES = Collections.unmodifiableList(signatures);
    }

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var operand = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        return new FunctionExpression(Func.TO_STRING, operand);
    }

}
