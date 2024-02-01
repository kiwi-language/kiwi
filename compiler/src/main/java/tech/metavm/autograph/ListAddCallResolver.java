package tech.metavm.autograph;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.ChildList;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Expressions;

import java.util.List;
import java.util.Objects;

import static tech.metavm.autograph.TranspileUtil.createType;

public class ListAddCallResolver implements MethodCallResolver {

    private static final PsiClassType CHILD_LIST_TYPE = createType(ChildList.class);

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(createType(List.class), "add",
                            TranspileUtil.createTypeVariableType(List.class, 0))
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression,
                              ExpressionResolver expressionResolver,
                              MethodGenerator methodGenerator) {
        var qualifier = Objects.requireNonNull(methodCallExpression.getMethodExpression().getQualifierExpression());
        var array = expressionResolver.resolve(qualifier);
        var value = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        if(CHILD_LIST_TYPE.isAssignableFrom(Objects.requireNonNull(qualifier.getType())))
            expressionResolver.processChildAssignment(array, null, value);
        methodGenerator.createAddElement(array, value);
        return Expressions.trueExpression();
    }
}
