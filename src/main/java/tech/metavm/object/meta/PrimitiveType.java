package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.rest.dto.PrimitiveTypeParam;

import java.util.List;
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

    @Override
    public int hashCode() {
        return Objects.hash(kind);
    }

    @Override
    protected PrimitiveTypeParam getParam() {
        return new PrimitiveTypeParam(kind.getCode());
    }

    public PrimitiveKind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return "PrimitiveType " + kind.getName();
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return kind.getJavaClass().getName();
    }
}
