package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.function.Function;

@EntityType("类型")
public abstract class Type extends Entity {

    public static final IndexDef<Type> UNIQUE_NAME = new IndexDef<>(Type.class, "name");

    public static final PrimitiveType NULL_TYPE = new PrimitiveType(PrimitiveKind.NULL);

    @EntityField(value = "名称", asTitle = true)
    protected String name;
    @EntityField(value = "编号")
    @Nullable
    private String code;
    @EntityField("是否匿名")
    protected boolean anonymous;
    @EntityField("是否临时")
    protected final boolean ephemeral;
    @EntityField("类别")
    protected final TypeCategory category;
    @EntityField("数组类型")
    @Nullable
    private ArrayType arrayType;
    @EntityField("可空类型")
    @Nullable
    private UnionType nullableType;

    public Type(String name, boolean anonymous, boolean ephemeral, TypeCategory category) {
        this.name = name;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.category = category;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public final TypeCategory getCategory() {
        return category;
    }

    public final boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public final boolean isEphemeral() {
        return ephemeral;
    }

    public final boolean isPersistent() {
        return !isEphemeral();
    }

    public final boolean isNullable() {
        return isAssignableFrom(NULL_TYPE);
    }

    public final boolean isNull() {
        return category.isNull();
    }

    public Type getConcreteType() {
        return this;
    }

    public abstract boolean isAssignableFrom(Type that);

    public boolean isInstance(Instance value) {
        return isAssignableFrom(value.getType());
    }

    public boolean contains(Type that) {
        return this == that;
    }

    public boolean isInt() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.INT;
    }

    public boolean isLong() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.LONG;
    }

    public boolean isDouble() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.DOUBLE;
    }

    public boolean isBoolean() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.BOOLEAN;
    }

    public boolean isString() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.STRING;
    }

    public boolean isTime() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.TIME;
    }

    public boolean isPassword() {
        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.PASSWORD;
    }

    public boolean isClass() {
        return (this instanceof ClassType klass) && klass.getCategory() == TypeCategory.CLASS;
    }

    public boolean isValue() {
        return (this instanceof ClassType klass) && klass.getCategory() == TypeCategory.VALUE;
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    public boolean isArray() {
        return this instanceof ArrayType;
    }

    public boolean isPrimitive() {
        return this instanceof PrimitiveType;
    }

    public boolean isEnum() {
        return this instanceof EnumType;
    }

    public boolean isReference() {
        return isClass() || isArray() || isEnum();
    }

    @Nullable
    public ArrayType getArrayType() {
        return arrayType;
    }

    public void setArrayType(@Nullable ArrayType arrayType) {
        this.arrayType = arrayType;
    }

    @Nullable
    public UnionType getNullableType() {
        return nullableType;
    }

    public Type getUnderlyingType() {
        if(this instanceof UnionType unionType) {
            if(isNullable()) {
                return NncUtils.findRequired(unionType.getTypeMembers(), t -> !t.equals(NULL_TYPE));
            }
        }
        return this;
    }

    public void setNullableType(@Nullable UnionType nullableType) {
        this.nullableType = nullableType;
    }

    public SQLType getSQLType() {
        return category.getSQLType();
    }

    protected TypeDTO toDTO(Object param) {
        return new TypeDTO(
                id,
                name,
                code,
                category.code(),
                ephemeral,
                anonymous,
                NncUtils.get(nullableType, Entity::getId),
                NncUtils.get(arrayType, Entity::getId),
                param
        );
    }

    public TypeDTO toDTO() {
        return toDTO(getParam());
    }

    protected abstract Object getParam();

    @Override
    public String toString() {
        return "Type " + name + " (id: " + getId() + ")";
    }

    public abstract String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType);

}
