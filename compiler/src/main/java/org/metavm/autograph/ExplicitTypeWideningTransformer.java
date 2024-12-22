package org.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtils.createExpressionFromText;

@Slf4j
public class ExplicitTypeWideningTransformer extends VisitorBase {

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
        var type = requireNonNull(expression.getType());
        var op = expression.getOperationTokenType();
        if(operators.contains(op)) {
            for (PsiExpression operand : expression.getOperands()) {
                processExpression(operand, type);
            }
        } else if(shiftOperators.contains(op)) {
            var operands = expression.getOperands();
            for (int i = 1; i < operands.length; i++) {
                processExpression(operands[i], TranspileUtils.intType);
            }
        }
    }

    @Override
    public void visitBinaryExpression(PsiBinaryExpression expression) {
        super.visitBinaryExpression(expression);
        var op = expression.getOperationSign().getTokenType();
        if (compareOperators.contains(op)) {
            var first = expression.getLOperand();
            var second = requireNonNull(expression.getROperand());
            if (first.getType() instanceof PsiPrimitiveType t1
                    && second.getType() instanceof PsiPrimitiveType t2) {
                int i1 = numericPrimitiveKinds.indexOf(t1.getKind());
                var i2 = numericPrimitiveKinds.indexOf(t2.getKind());
                if(i1 < i2)
                    processExpression(second, t1);
                else if(i2 < i1)
                    processExpression(first, t2);
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
        var args = requireNonNull(call.getArgumentList()).getExpressions();
        var params = requireNonNull(call.resolveMethod()).getParameterList().getParameters();
        for (int i = 0; i < args.length; i++) {
            processExpression(args[i], params[i].getType());
        }
    }

    @Override
    public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
        super.visitArrayInitializerExpression(expression);
        var elementType = ((PsiArrayType) requireNonNull(expression.getType())).getComponentType();
        if(elementType instanceof PsiPrimitiveType) {
            for (PsiExpression initializer : expression.getInitializers()) {
                processExpression(initializer, elementType);
            }
        }
    }

    @Override
    public void visitVariable(PsiVariable variable) {
        super.visitVariable(variable);
        if (variable.getInitializer() != null) {
            processExpression(variable.getInitializer(), variable.getType());
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        super.visitAssignmentExpression(expression);
        var op = expression.getOperationSign().getTokenType();
        var assignment = requireNonNull(expression.getRExpression());
        if (shiftOperators.contains(op))
            processExpression(assignment, TranspileUtils.intType);
        else
            processExpression(assignment, expression.getType());
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
        processExpression(requireNonNull(statement.getExpression()), switchExpr.getType());
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
        if (expression.getType() instanceof PsiPrimitiveType lType
                && assignedType instanceof PsiPrimitiveType rType && !lType.equals(rType)) {
                replace(expression, createExpressionFromText(String.format("(%s) (%s)",
                        rType.getName(), expression.getText())));
        }
    }

}
