package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class UnaryExpression extends Expression {
    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
    private final UnaryOperator operator;
    private final Expression operand;

    public UnaryExpression(@NotNull UnaryOperator operator, @NotNull Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Generated
    public static UnaryExpression read(MvInput input) {
        return new UnaryExpression(UnaryOperator.fromCode(input.read()), Expression.read(input));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitByte();
        Expression.visit(visitor);
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
    public List<Expression> getComponents() {
        return List.of(operand);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        operand.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        operand.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("operator", this.getOperator().name());
        map.put("operand", this.getOperand().toJson());
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_UnaryExpression);
        super.write(output);
        output.write(operator.code());
        operand.write(output);
    }
}
