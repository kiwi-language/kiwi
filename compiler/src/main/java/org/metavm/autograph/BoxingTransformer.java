package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

import static org.metavm.autograph.TranspileUtils.createExpressionFromText;

@Slf4j
public class BoxingTransformer extends VisitorBase {

    @Override
    public void visitEnumConstant(PsiEnumConstant enumConstant) {
        super.visitEnumConstant(enumConstant);
        processCall(enumConstant);
    }
    @Override
    public void visitCallExpression(PsiCallExpression callExpression) {
        super.visitCallExpression(callExpression);
        if(callExpression instanceof PsiNewExpression newExpression && newExpression.isArrayCreation())
            return;
        processCall(callExpression);
    }

    private void processCall(PsiCall call) {
        var args = Objects.requireNonNull(call.getArgumentList()).getExpressions();
        var method = Objects.requireNonNull(call.resolveMethod(),
                () -> "Failed to resolve method for call expression: " + call.getText());
        var params = method.getParameterList().getParameters();
        for (int i = 0; i < args.length; i++) {
            tryBox(args[i], params[i].getType());
        }
    }

    @Override
    public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
        super.visitArrayInitializerExpression(expression);
        var elementType = ((PsiArrayType) Objects.requireNonNull(expression.getType())).getComponentType();
        if(elementType instanceof PsiPrimitiveType) {
            for (PsiExpression initializer : expression.getInitializers()) {
                tryBox(initializer, elementType);
            }
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitAssignmentExpression(expression);
        var assignment = Objects.requireNonNull(expression.getRExpression());
        tryBox(assignment, expression.getType());
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
        super.visitReturnStatement(statement);
        var value = statement.getReturnValue();
        if(value != null) {
            var parent = TranspileUtils.findParent(statement, Set.of(PsiMethod.class, PsiLambdaExpression.class));
            var returnType = switch (parent) {
                case PsiLambdaExpression lambdaExpression -> TranspileUtils.getLambdaReturnType(lambdaExpression);
                case PsiMethod method -> method.getReturnType();
                case null, default -> throw new IllegalStateException(
                        "Cannot find an enclosing callable for return statement '" + statement.getText() + "'"
                );
            };
            tryBox(value, returnType);
        }
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        super.visitYieldStatement(statement);
        var switchExpr = TranspileUtils.getParent(statement, PsiSwitchExpression.class);
        tryBox(statement.getExpression(), switchExpr.getType());
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        super.visitSwitchLabeledRuleStatement(statement);
        if(statement.getBody() instanceof PsiExpressionStatement exprStmt
                &&  TranspileUtils.findParent(statement, Set.of(PsiSwitchExpression.class, PsiSwitchStatement.class))
                instanceof PsiSwitchExpression switchExpr) {
            tryBox(exprStmt.getExpression(), switchExpr.getType());
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        super.visitLambdaExpression(expression);
        if(expression.getBody() instanceof PsiExpression bodyExpr)
            tryBox(bodyExpr, TranspileUtils.getLambdaReturnType(expression));
    }

    private void tryBox(PsiExpression expression, PsiType assignedType) {
        if (!(assignedType instanceof PsiPrimitiveType)) {
            var type = Objects.requireNonNull(expression.getType());
            if (type.equals(PsiType.LONG))
                replace(expression, createExpressionFromText("Long.valueOf(" + expression.getText() + ")"));
            else if (type.equals(PsiType.INT))
                replace(expression, createExpressionFromText("Integer.valueOf(" + expression.getText() +  ")"));
            else if (type.equals(PsiType.DOUBLE))
                replace(expression, createExpressionFromText("Double.valueOf(" + expression.getText() + ")"));
            else if (type.equals(PsiType.FLOAT))
                replace(expression, createExpressionFromText("Float.valueOf(" + expression.getText() + ")"));
            else if (type.equals(PsiType.SHORT))
                replace(expression, createExpressionFromText("Short.valueOf(" + expression.getText() + ")"));
            else if (type.equals(PsiType.BYTE))
                replace(expression, createExpressionFromText("Byte.valueOf(" + expression.getText() + ")"));
            else if (type.equals(PsiType.CHAR))
                replace(expression, createExpressionFromText("Character.valueOf(" + expression.getText() + ")"));
            else if (type.equals(PsiType.BOOLEAN))
                replace(expression, createExpressionFromText("Boolean.valueOf(" + expression.getText() + ")"));
        }
    }

}
