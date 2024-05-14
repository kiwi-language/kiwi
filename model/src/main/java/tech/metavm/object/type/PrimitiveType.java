package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.PrimitiveTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;
import java.util.function.Function;

@EntityType("基础类型")
public class PrimitiveType extends Type {

    @EntityField("分类")
    private final PrimitiveKind kind;

    public PrimitiveType(PrimitiveKind kind) {
        super();
        this.kind = kind;
    }

    @Override
    public String getName() {
        return kind.getName();
    }

    @Override
    public @NotNull String getCode() {
        return kind.getTypeCode();
    }

    @Override
    public TypeCategory getCategory() {
        return kind.getTypeCategory();
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, String> getTypeDefId) {
        return new PrimitiveTypeKey(kind.code());
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return equals(that);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitPrimitiveType(this, s);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof PrimitiveType that && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind);
    }

    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isNull() {
        return kind == PrimitiveKind.NULL;
    }

    @Override
    public boolean isLong() {
        return kind == PrimitiveKind.LONG;
    }

    @Override
    public boolean isDouble() {
        return kind == PrimitiveKind.DOUBLE;
    }

    @Override
    public boolean isNumber() {
        return kind == PrimitiveKind.DOUBLE || kind == PrimitiveKind.LONG;
    }

    @Override
    public boolean isBoolean() {
        return kind == PrimitiveKind.BOOLEAN;
    }

    @Override
    public boolean isString() {
        return kind == PrimitiveKind.STRING;
    }

    @Override
    public boolean isVoid() {
        return kind == PrimitiveKind.VOID;
    }

    @Override
    public boolean isTime() {
        return kind == PrimitiveKind.TIME;
    }

    @Override
    public boolean isPassword() {
        return kind == PrimitiveKind.PASSWORD;
    }

    public PrimitiveKind getKind() {
        return kind;
    }

    @Override
    protected String toString0() {
        return "PrimitiveType " + kind.getName();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return kind.getTypeCode();
    }

    @Override
    public PrimitiveType copy() {
        return new PrimitiveType(kind);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<TypeDef, String> getTypeDefExpr) {
        return kind.name().toLowerCase();
    }

    @Override
    public int getTypeKeyCode() {
        return PrimitiveTypeKey.getTypeKeyCode(kind.code());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPrimitiveType(this);
    }

    @Override
    public void write0(InstanceOutput output) {
        output.write(kind.code());
    }

    public static PrimitiveType read(InstanceInput input) {
        return new PrimitiveType(PrimitiveKind.fromCode(input.readInt()));
    }

}
