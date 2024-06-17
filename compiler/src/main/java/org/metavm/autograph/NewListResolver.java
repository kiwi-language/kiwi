package org.metavm.autograph;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiNewExpression;
import org.metavm.api.ChildList;
import org.metavm.common.ErrorCode;
import org.metavm.expression.Expression;
import org.metavm.expression.NodeExpression;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.util.BusinessException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class NewListResolver implements NewResolver {

    public static final PsiClassType CHILD_LIST_TYPE = TranspileUtils.createClassType(ChildList.class);

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(CHILD_LIST_TYPE, "ChildList"),
                    MethodSignature.create(TranspileUtils.createClassType(ArrayList.class), "ArrayList"),
                    MethodSignature.create(TranspileUtils.createClassType(LinkedList.class), "LinkedList")
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiNewExpression methodCallExpression, ExpressionResolver expressionResolver, MethodGenerator methodGenerator) {
        var methodGenerics = methodCallExpression.resolveMethodGenerics();
        var type = (PsiClassType) Objects.requireNonNull(methodCallExpression.getType());
        if (type.getParameters().length == 0)
            throw new BusinessException(ErrorCode.RAW_TYPES_NOT_SUPPORTED);
        var elementType = methodGenerics.getSubstitutor().substitute(type.getParameters()[0]);
        var typeResolver = expressionResolver.getTypeResolver();
        var mvElementType = typeResolver.resolve(elementType);
        var mvArrayType = new ArrayType(mvElementType,
                CHILD_LIST_TYPE.isAssignableFrom(type) ? ArrayKind.CHILD : ArrayKind.READ_WRITE);
        return new NodeExpression(methodGenerator.createNewArray(mvArrayType, null));
    }
}
