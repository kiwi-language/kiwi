package tech.metavm.autograph;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiNewExpression;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.ChildList;
import tech.metavm.expression.Expression;
import tech.metavm.expression.NodeExpression;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.util.BusinessException;

import java.util.*;

public class NewListWithInitialResolver implements NewResolver {

    public static final PsiClassType CHILD_LIST_TYPE = TranspileUtil.createType(ChildList.class);

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(CHILD_LIST_TYPE,
                            "ChildList", TranspileUtil.createType(
                                    Collection.class,
                                    TranspileUtil.createExtendsWildcardType(
                                            TranspileUtil.createTypeVariableType(ChildList.class, 0)
                                    )
                            )),
                    MethodSignature.create(TranspileUtil.createType(ArrayList.class),
                            "ArrayList",
                            TranspileUtil.createType(
                                    Collection.class,
                                    TranspileUtil.createExtendsWildcardType(
                                            TranspileUtil.createTypeVariableType(ArrayList.class, 0)
                                    )
                            )
                    ),
                    MethodSignature.create(TranspileUtil.createType(LinkedList.class),
                            "LinkedList",
                            TranspileUtil.createType(
                                    Collection.class,
                                    TranspileUtil.createExtendsWildcardType(
                                            TranspileUtil.createTypeVariableType(LinkedList.class, 0)
                                    )
                            )
                    )
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
        var mvArrayType = expressionResolver.getArrayTypeProvider().getArrayType(mvElementType,
                CHILD_LIST_TYPE.isAssignableFrom(type) ? ArrayKind.CHILD : ArrayKind.READ_WRITE);
        var initialValue = expressionResolver.resolve(
                Objects.requireNonNull(methodCallExpression.getArgumentList()).getExpressions()[0]);
        return new NodeExpression(methodGenerator.createNewArray(mvArrayType, initialValue));
    }
}
