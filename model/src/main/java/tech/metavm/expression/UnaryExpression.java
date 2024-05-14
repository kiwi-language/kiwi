package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("一元表达式")
public class UnaryExpression extends Expression {
    @EntityField("运算符")
    private final UnaryOperator operator;
    @ChildEntity("运算数")
    private final Expression operand;

    public UnaryExpression(@NotNull UnaryOperator operator, @NotNull Expression operand) {
        this.operator = operator;
        this.operand = addChild(operand.copy(), "operand");
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        boolean operandParenthesized = operand.precedence() >= precedence();
        String operandExpr = operand.build(symbolType, operandParenthesized, relaxedCheck);
        if(operator.isPrefix()) {
            return operator.operator() + " " + operandExpr;
        }
        else {
            return operandExpr + " " + operator.operator();
        }
    }

//    @Override
//    public Expression simplify() {
//        var operand = this.operand.simplify();;
//        if(operator == UnaryOperator.NOT) {
//            if(operand instanceof ConstantExpression constExpr)
//                return new ConstantExpression(((BooleanInstance) constExpr.getValue()).not());
//            else if(operand instanceof UnaryExpression unaryExpr && unaryExpr.getOperator() == UnaryOperator.NOT)
//                return unaryExpr.operand;
//        }
//        else if(operator == UnaryOperator.NEG) {
//            if(operand instanceof ConstantExpression constExpr)
//                return new ConstantExpression(((NumberInstance) constExpr.getValue()).negate());
//        }
//        return new UnaryExpression(operator, operand);
//    }

    @Override
    public int precedence() {
        return operator.precedence();
    }

    @Override
    public Type getType() {
        if(operator.resultType() != null)
            return operator.resultType();
        else
            return operand.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(operand);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return operator.evaluate(operand.evaluate(context));
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return operand.extractExpressions(klass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnaryExpression that)) return false;
        return operator == that.operator && Objects.equals(operand, that.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, operand);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}
