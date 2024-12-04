package org.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.metavm.autograph.TranspileUtils.createExpressionFromText;

@Slf4j
public class UnboxingTransformer extends VisitorBase {

    public static final Set<IElementType> operators = Set.of(
            JavaTokenType.PLUS,
            JavaTokenType.MINUS,
            JavaTokenType.ASTERISK,
            JavaTokenType.DIV,
            JavaTokenType.PERC,
            JavaTokenType.AND,
            JavaTokenType.OR,
            JavaTokenType.XOR,
            JavaTokenType.PLUSEQ,
            JavaTokenType.MINUSEQ,
            JavaTokenType.ASTERISKEQ,
            JavaTokenType.DIVEQ,
            JavaTokenType.PERCEQ,
            JavaTokenType.ANDEQ,
            JavaTokenType.OREQ,
            JavaTokenType.XOREQ
    );

    public static final Set<IElementType> shiftOperators = Set.of(
            JavaTokenType.LTLT,
            JavaTokenType.GTGT,
            JavaTokenType.GTGTGT,
            JavaTokenType.LTLTEQ,
            JavaTokenType.GTGTEQ,
            JavaTokenType.GTGTGTEQ
    );

    public static final Set<IElementType> compareOperators = Set.of(
            JavaTokenType.GT,
            JavaTokenType.GE,
            JavaTokenType.LT,
            JavaTokenType.LE,
            JavaTokenType.EQEQ
    );

    public static final List<JvmPrimitiveTypeKind> numericPrimitiveKinds = List.of(
        JvmPrimitiveTypeKind.DOUBLE, JvmPrimitiveTypeKind.FLOAT, JvmPrimitiveTypeKind.LONG, JvmPrimitiveTypeKind.INT,
        JvmPrimitiveTypeKind.SHORT, JvmPrimitiveTypeKind.CHAR, JvmPrimitiveTypeKind.BYTE
    );

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        var type = expression.getType();
        var op = expression.getOperationTokenType();
        if(operators.contains(op)) {
            for (PsiExpression operand : expression.getOperands()) {
                tryUnbox(operand, type);
            }
        } else if(shiftOperators.contains(op)) {
            var operands = expression.getOperands();
            tryUnbox(operands[0], type);
            for (int i = 1; i < operands.length; i++) {
                tryUnbox(operands[i], TranspileUtils.intType);
            }
        }
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        super.visitBinaryExpression(expression);
        var op = expression.getOperationSign().getTokenType();
        if (compareOperators.contains(op)) {
            var first = expression.getLOperand();
            var second = Objects.requireNonNull(expression.getROperand());
            tryUnbox(second, first.getType());
            tryUnbox(first, second.getType());
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
            tryUnbox(args[i], params[i].getType());
        }
    }

    @Override
    public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
        super.visitArrayInitializerExpression(expression);
        var elementType = ((PsiArrayType) Objects.requireNonNull(expression.getType())).getComponentType();
        if(elementType instanceof PsiPrimitiveType) {
            for (PsiExpression initializer : expression.getInitializers()) {
                tryUnbox(initializer, elementType);
            }
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitAssignmentExpression(expression);
        var op = expression.getOperationSign().getTokenType();
        var assignment = Objects.requireNonNull(expression.getRExpression());
        if (shiftOperators.contains(op))
            tryUnbox(assignment, TranspileUtils.intType);
        else
            tryUnbox(assignment, expression.getType());
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
            tryUnbox(value, returnType);
        }
    }

    @Override
    public void visitYieldStatement(PsiYieldStatement statement) {
        super.visitYieldStatement(statement);
        var switchExpr = TranspileUtils.getParent(statement, PsiSwitchExpression.class);
        tryUnbox(statement.getExpression(), switchExpr.getType());
    }

    @Override
    public void visitSwitchLabeledRuleStatement(PsiSwitchLabeledRuleStatement statement) {
        super.visitSwitchLabeledRuleStatement(statement);
        if(statement.getBody() instanceof PsiExpressionStatement exprStmt
                &&  TranspileUtils.findParent(statement, Set.of(PsiSwitchExpression.class, PsiSwitchStatement.class))
                instanceof PsiSwitchExpression switchExpr) {
            tryUnbox(exprStmt.getExpression(), switchExpr.getType());
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        super.visitLambdaExpression(expression);
        if(expression.getBody() instanceof PsiExpression bodyExpr)
            tryUnbox(bodyExpr, TranspileUtils.getLambdaReturnType(expression));
    }

    private void tryUnbox(PsiExpression expression, PsiType assignedType) {
        if (assignedType instanceof PsiPrimitiveType) {
            var type = expression.getType();
            if (TranspileUtils.isLongWrapperType(type)) {
                replace(expression, createExpressionFromText(expression.getText() + ".longValue()"));
            } else if (TranspileUtils.isIntWrapperType(type)) {
                replace(expression, createExpressionFromText(expression.getText() + ".intValue()"));
            } else if (TranspileUtils.isDoubleWrapperType(type)) {
                replace(expression, createExpressionFromText(expression.getText() + ".doubleValue()"));
            }
        }
    }

}
