package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public class StringConcatTransformer extends VisitorBase {

    private final PsiClassType stringType = TranspileUtil.createType(String.class);

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        expression = (PsiPolyadicExpression) getReplacement(expression);
        var op = expression.getOperationTokenType();
        if (op == JavaTokenType.PLUS && NncUtils.anyMatch(
                List.of(expression.getOperands()),
                operand -> Objects.requireNonNull(operand.getType()).equals(stringType)
        )) {
            var buf = new StringBuilder();
            for (PsiExpression operand : expression.getOperands()) {
                operand = convertToString(operand);
                if (buf.isEmpty())
                    buf.append(operand.getText());
                else
                    buf.append(".concat(").append(operand.getText()).append(')');
            }
            replace(expression, TranspileUtil.createExpressionFromText(buf.toString()));
        }
    }

    private PsiExpression convertToString(PsiExpression expression) {
        var type = Objects.requireNonNull(expression.getType());
        if (type.equals(stringType))
            return expression;
        var primitiveTypes = TranspileUtil.getPrimitiveTypes();
        for (PsiPrimitiveType primitiveType : primitiveTypes) {
            if (type.equals(primitiveType)) {
                return (PsiExpression) replace(expression, TranspileUtil.createExpressionFromText(
                        String.format("%s.toString(%s)", primitiveType.getBoxedTypeName(), expression.getText())
                ));
            }
        }
        return (PsiExpression) replace(
                expression,
                TranspileUtil.createExpressionFromText(
                        String.format("%s.toString()", expression.getText())
                )
        );
    }

}
