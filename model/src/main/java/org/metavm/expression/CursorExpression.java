package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class CursorExpression extends Expression{

    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;


    private final Type type;
    private final @Nullable String alias;

    public CursorExpression(Type type, @Nullable String alias) {
        this.type = type;
        this.alias = alias;
    }

    @Generated
    public static CursorExpression read(MvInput input) {
        return new CursorExpression(input.readType(), input.readNullable(input::readUTF));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitNullable(visitor::visitUTF);
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return alias;
    }

    public @Nullable String getAlias() {
        return alias;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CursorExpression that)) return false;
        return Objects.equals(type, that.type) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, alias);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCursorExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        type.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        var alias = this.getAlias();
        if (alias != null) map.put("alias", alias);
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_CursorExpression);
        super.write(output);
        output.writeValue(type);
        output.writeNullable(alias, output::writeUTF);
    }
}
