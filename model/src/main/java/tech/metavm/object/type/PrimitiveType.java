package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.PrimitiveTypeKey;
import tech.metavm.object.type.rest.dto.PrimitiveTypeParam;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.util.Objects;

@EntityType("基础类型")
public class PrimitiveType extends Type {

    @EntityField("分类")
    private final PrimitiveKind kind;

    public PrimitiveType(PrimitiveKind kind) {
        super(kind.getName(), kind.getTypeCode(), false, true, kind.getTypeCategory());
        this.kind = kind;
    }

    @Override
    public TypeKey toTypeKey() {
        return new PrimitiveTypeKey(kind.code());
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return equals(that);
    }

    @Override
    public boolean equals(Object obj) {
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

    @Override
    protected PrimitiveTypeParam getParam(SerializeContext serializeContext) {
        return new PrimitiveTypeParam(kind.code());
    }

    public PrimitiveKind getKind() {
        return kind;
    }

    @Override
    protected String toString0() {
        return "PrimitiveType " + kind.getName();
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return kind.getJavaClass().getName();
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
    public String toTypeExpression(SerializeContext serializeContext) {
        return kind.name().toLowerCase();
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
