package org.metavm.autograph;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

import static org.metavm.autograph.TranspileUtils.createExpressionFromText;

@Slf4j
public class PrimitiveConversionTransformer extends VisitorBase {

    public static final Set<IElementType> operators = Set.of(
            JavaTokenType.PLUS,
            JavaTokenType.MINUS,
            JavaTokenType.ASTERISK,
            JavaTokenType.DIV,
            JavaTokenType.PERC,
            JavaTokenType.PLUSEQ,
            JavaTokenType.MINUSEQ,
            JavaTokenType.ASTERISKEQ,
            JavaTokenType.DIVEQ,
            JavaTokenType.PERCEQ
    );

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        var type = expression.getType();
        if(operators.contains(expression.getOperationTokenType())) {
            for (PsiExpression operand : expression.getOperands()) {
                processExpression(operand, type);
            }
        }
    }

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
        var params = Objects.requireNonNull(call.resolveMethod()).getParameterList().getParameters();
        for (int i = 0; i < args.length; i++) {
            processExpression(args[i], params[i].getType());
        }
    }

    @Override
    public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
        super.visitArrayInitializerExpression(expression);
        var elementType = ((PsiArrayType) Objects.requireNonNull(expression.getType())).getComponentType();
        if(elementType instanceof PsiPrimitiveType) {
            for (PsiExpression initializer : expression.getInitializers()) {
                processExpression(initializer, elementType);
            }
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitAssignmentExpression(expression);
        processExpression(Objects.requireNonNull(expression.getRExpression()), expression.getType());
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
            processExpression(value, returnType);
        }
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        super.visitYieldStatement(statement);
        var switchExpr = TranspileUtils.getParent(statement, PsiSwitchExpression.class);
        processExpression(statement.getExpression(), switchExpr.getType());
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        super.visitSwitchLabeledRuleStatement(statement);
        if(statement.getBody() instanceof PsiExpressionStatement exprStmt
                &&  TranspileUtils.findParent(statement, Set.of(PsiSwitchExpression.class, PsiSwitchStatement.class))
                instanceof PsiSwitchExpression switchExpr) {
            processExpression(exprStmt.getExpression(), switchExpr.getType());
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        super.visitLambdaExpression(expression);
        if(expression.getBody() instanceof PsiExpression bodyExpr)
            processExpression(bodyExpr, TranspileUtils.getLambdaReturnType(expression));
    }

    private void processExpression(PsiExpression expression, PsiType assignedType) {
        if(TranspileUtils.isLongType(assignedType)) {
            if(!TranspileUtils.isLongType(expression.getType()))
                replace(expression, createExpressionFromText("(long) (" + expression.getText() + ")"));
        }
        else if(TranspileUtils.isDoubleType(assignedType)) {
            if(!TranspileUtils.isDoubleType(expression.getType()))
                replace(expression, createExpressionFromText("(double) (" + expression.getText() + ")"));
        }
    }

}
