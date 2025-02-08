package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class FunctionExpression extends Expression {
    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
    private final Func function;
    private final List<Expression> arguments;

    public FunctionExpression(Func function, List<Expression> arguments) {
        this.function = function;
        this.arguments = new ArrayList<>(arguments);
    }

    public FunctionExpression(Func function, Expression argument) {
        this.function = function;
        List<Expression> expressions = argument instanceof ArrayExpression arrayExpression ?
                arrayExpression.getExpressions() : List.of(argument);
        arguments = new ArrayList<>(expressions);
    }

    @Generated
    public static FunctionExpression read(MvInput input) {
        return new FunctionExpression(Func.fromCode(input.read()), input.readList(() -> Expression.read(input)));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitByte();
        visitor.visitList(() -> Expression.visit(visitor));
    }

    public Func getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return function.name() + "(" + Utils.join(arguments, arg -> arg.buildSelf(symbolType, relaxedCheck), ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return function.getReturnType(Utils.map(arguments, Expression::getType));
    }

    @Override
    public List<Expression> getComponents() {
        return Utils.listOf(arguments);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return function.evaluate(Utils.map(arguments, arg -> arg.evaluate(context)));
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return Utils.flatMap(arguments, arg -> arg.extractExpressions(klass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionExpression that)) return false;
        return function == that.function && Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, arguments);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        arguments.forEach(arg -> arg.accept(visitor));
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        for (var arguments_ : arguments) arguments_.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("function", this.getFunction().name());
        map.put("arguments", this.getArguments().stream().map(Expression::toJson).toList());
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_FunctionExpression);
        super.write(output);
        output.write(function.code());
        output.writeList(arguments, arg0 -> arg0.write(output));
    }
}
