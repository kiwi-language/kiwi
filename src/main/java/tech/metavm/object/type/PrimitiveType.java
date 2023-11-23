package tech.metavm.object.type;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.rest.dto.PrimitiveTypeKey;
import tech.metavm.object.type.rest.dto.PrimitiveTypeParam;
import tech.metavm.object.type.rest.dto.TypeKey;

import java.util.Objects;
import java.util.function.Function;

@EntityType("基础类型")
public class PrimitiveType extends Type {

    @EntityField("分类")
    private final PrimitiveKind kind;

    public PrimitiveType(PrimitiveKind kind) {
        super(kind.getName(), false, true, kind.getTypeCategory());
        setCode(kind.name());
        this.kind = kind;
    }

    @Override
    public TypeKey getTypeKey() {
        return new PrimitiveTypeKey(kind.code());
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return equals(that);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveType that = (PrimitiveType) o;
        return kind == that.kind;
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
    public int hashCode() {
        return Objects.hash(kind);
    }

    @Override
    protected PrimitiveTypeParam getParam() {
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
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return kind.getJavaClass().getName();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
