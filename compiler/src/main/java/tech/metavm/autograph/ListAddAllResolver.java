package tech.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.Expression;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.Func;
import tech.metavm.flow.AddElementNode;
import tech.metavm.flow.ValueNode;
import tech.metavm.flow.Values;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

public class ListAddAllResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES =
            List.of(
                    MethodSignature.create(TranspileUtil.createClassType(List.class), "addAll",
                            TranspileUtil.createType(
                                    Collection.class,
                                    TranspileUtil.createExtendsWildcardType(
                                            TranspileUtil.createTypeVariableType(List.class, 0)
                                    )
                            )
                    )
            );

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Expression resolve(PsiMethodCallExpression methodCallExpression,
                              ExpressionResolver expressionResolver,
                              MethodGenerator methodGenerator) {
        var self = expressionResolver.resolve(methodCallExpression.getMethodExpression().getQualifierExpression());
        var array = expressionResolver.resolve(methodCallExpression.getArgumentList().getExpressions()[0]);
        var loop = methodGenerator.createWhile();
        var indexField = FieldBuilder.newBuilder("索引", null, loop.getKlass(), StandardTypes.getLongType())
                .build();
        loop.setCondition(
                Values.expression(
                        Expressions.lt(
                                Expressions.nodeProperty(loop, indexField),
                                Expressions.func(Func.LEN, array)
                        )
                )
        );
        loop.setField(
                indexField, Values.constantLong(0L),
                Values.expression(
                        Expressions.add(
                                Expressions.nodeProperty(loop, indexField),
                                Expressions.constantLong(1L)
                        )
                )
        );
        var element = new ValueNode(
                null, "元素" + NncUtils.randomNonNegative(), null,
                ((ArrayType) array.getType()).getElementType(),
                loop.getBodyScope().getLastNode(), loop.getBodyScope(),
                Values.expression(
                        Expressions.arrayAccess(array, Expressions.nodeProperty(loop, indexField))
                )
        );
        new AddElementNode(
                null,"添加元素" + NncUtils.randomNonNegative(), null,
                loop.getBodyScope().getLastNode(), loop.getBodyScope(),
                Values.expression(self), Values.node(element)
        );
        return Expressions.trueExpression();
    }
}
