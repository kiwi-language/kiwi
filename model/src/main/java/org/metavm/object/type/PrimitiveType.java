package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.rest.dto.PrimitiveTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.InstanceOutput;

import java.util.Objects;
import java.util.function.Function;

@EntityType
public class PrimitiveType extends Type {

    public static final PrimitiveType nullType = new PrimitiveType(PrimitiveKind.NULL);
    public static final PrimitiveType doubleType = new PrimitiveType(PrimitiveKind.DOUBLE);
    public static final PrimitiveType longType = new PrimitiveType(PrimitiveKind.LONG);
    public static final PrimitiveType booleanType = new PrimitiveType(PrimitiveKind.BOOLEAN);
    public static final PrimitiveType stringType = new PrimitiveType(PrimitiveKind.STRING);
    public static final PrimitiveType timeType = new PrimitiveType(PrimitiveKind.TIME);
    public static final PrimitiveType voidType = new PrimitiveType(PrimitiveKind.VOID);
    public static final PrimitiveType passwordType = new PrimitiveType(PrimitiveKind.PASSWORD);

    private final PrimitiveKind kind;

    private PrimitiveType(PrimitiveKind kind) {
        super();
        this.kind = kind;
        kind.setType(this);
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
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
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

    @Override
    protected boolean isConvertibleFrom0(Type that) {
        return that instanceof PrimitiveType pt && kind.isConvertibleFrom(pt.kind);
    }

    @Override
    public Value convert(Value instance) {
        if(isConvertibleFrom(instance.getType()))
            return kind.convert(instance);
        else
            throw new IllegalArgumentException(instance.getType() + " is not convertible to " + this);
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
    public String toExpression(SerializeContext serializeContext, @javax.annotation.Nullable Function<ITypeDef, String> getTypeDefExpr) {
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
    public void write(InstanceOutput output) {
        output.write(PrimitiveTypeKey.getTypeKeyCode(kind.code()));
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    public @Nullable Value getDefaultValue() {
        return kind.getDefaultValue();
    }
}
