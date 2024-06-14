package org.metavm.autograph;

import com.intellij.psi.PsiMethodCallExpression;
import org.metavm.entity.StandardTypes;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.expression.Func;
import org.metavm.flow.AddElementNode;
import org.metavm.flow.ValueNode;
import org.metavm.flow.Values;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.FieldBuilder;

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
        var indexField = FieldBuilder.newBuilder("index", null, loop.getKlass(), StandardTypes.getLongType())
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
        var bodyScope = loop.getBodyScope();
        var element = new ValueNode(
                null, bodyScope.nextNodeName("element"), null,
                ((ArrayType) array.getType()).getElementType(),
                bodyScope.getLastNode(), bodyScope,
                Values.expression(
                        Expressions.arrayAccess(array, Expressions.nodeProperty(loop, indexField))
                )
        );
        new AddElementNode(
                null,bodyScope.nextNodeName("addElement"), null,
                bodyScope.getLastNode(), bodyScope,
                Values.expression(self), Values.node(element)
        );
        return Expressions.trueExpression();
    }
}
