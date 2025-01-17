package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Entity
public abstract class Value implements ValueObject {

    protected static final int TYPE_ConstantValue = 0;
    protected static final int TYPE_ArrayValue = 1;
    protected static final int TYPE_ExpressionValue = 2;
    protected static final int TYPE_NeverValue = 3;
    protected static final int TYPE_NodeValue = 4;
    protected static final int TYPE_PropertyValue = 5;
    protected static final int TYPE_TypeValue = 6;

    public Value() {
        super();
    }

    @Generated
    public static Value read(MvInput input) {
        var type = input.read();
        return switch (type) {
            case TYPE_ExpressionValue -> ExpressionValue.read(input);
            case TYPE_PropertyValue -> PropertyValue.read(input);
            case TYPE_ConstantValue -> ConstantValue.read(input);
            case TYPE_NeverValue -> NeverValue.read(input);
            case TYPE_ArrayValue -> ArrayValue.read(input);
            case TYPE_TypeValue -> TypeValue.read(input);
            case TYPE_NodeValue -> NodeValue.read(input);
            default -> throw new IllegalStateException("Unrecognized type: " + type);
        };
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        var type = visitor.visitByte();
        switch (type) {
            case TYPE_ExpressionValue -> ExpressionValue.visit(visitor);
            case TYPE_PropertyValue -> PropertyValue.visit(visitor);
            case TYPE_ConstantValue -> ConstantValue.visit(visitor);
            case TYPE_NeverValue -> NeverValue.visit(visitor);
            case TYPE_ArrayValue -> ArrayValue.visit(visitor);
            case TYPE_TypeValue -> TypeValue.visit(visitor);
            case TYPE_NodeValue -> NodeValue.visit(visitor);
            default -> throw new IllegalStateException("Unrecognized type: " + type);
        }
    }

    public abstract Type getType();

    @NotNull
    public abstract org.metavm.object.instance.core.Value evaluate(EvaluationContext context);

    public abstract String getText();

    public abstract Expression getExpression();

    public void forEachReference(Consumer<Reference> action) {
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("text", this.getText());
        map.put("expression", this.getExpression().toJson());
    }

    @Generated
    public void write(MvOutput output) {
    }

    public java.util.Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
