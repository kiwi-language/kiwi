package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Objects;

public class StringConcatTransformer extends SkipDiscardedVisitor {

    private final PsiClassType stringType = TranspileUtils.createClassType(String.class);

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        expression = (PsiPolyadicExpression) getReplacement(expression);
        var op = expression.getOperationTokenType();
        if (op == JavaTokenType.PLUS && Utils.anyMatch(
                List.of(expression.getOperands()),
                operand -> Objects.requireNonNull(operand.getType()).equals(stringType)
        )) {
            var sb = new StringBuilder();
            var operands = expression.getOperands();
            sb.append("org.metavm.api.lang.Lang.concat(".repeat(operands.length - 1));
            sb.append(operands[0].getText());
            for (int i = 1; i < operands.length; i++)
               sb.append(',').append(operands[i].getText()).append(')');
            replace(expression, TranspileUtils.createExpressionFromText(sb.toString()));
        }
    }

    private PsiExpression convertToString(PsiExpression expression) {
        var type = Objects.requireNonNull(expression.getType());
        if (type.equals(stringType))
            return expression;
        var primitiveTypes = TranspileUtils.getPrimitiveTypes();
        for (PsiPrimitiveType primitiveType : primitiveTypes) {
            if (type.equals(primitiveType)) {
                return (PsiExpression) replace(expression, TranspileUtils.createExpressionFromText(
                        String.format("%s.toString(%s)", primitiveType.getBoxedTypeName(), expression.getText())
                ));
            }
        }
        return (PsiExpression) replace(
                expression,
                TranspileUtils.createExpressionFromText(
                        String.format("%s.toString()", expression.getText())
                )
        );
    }

}
