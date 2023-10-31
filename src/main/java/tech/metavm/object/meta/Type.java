package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.object.meta.rest.dto.TypeParam;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@EntityType("类型")
public abstract class Type extends Entity {

    public static PrimitiveType NULL_TYPE = new PrimitiveType(PrimitiveKind.NULL);

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
    protected TypeCategory category;
    @EntityField("是否模版")
    private boolean isTemplate;
    @EntityField("是否参数化")
    private boolean isParameterized;

    public Type(String name, boolean anonymous, boolean ephemeral, TypeCategory category) {
        this.name = name;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public String getCodeRequired() {
        return NncUtils.requireNonNull(code, "Code is not set for type '" + name + "'");
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public TypeCategory getCategory() {
        return category;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isPersistent() {
        return !isEphemeral();
    }

    public Type getCertainUpperBound() {
        if(!(this instanceof UncertainType)) {
            return this;
        }
        var current = getUpperBound();
        Set<Type> visited = new IdentitySet<>();
        while (current instanceof UncertainType) {
            if(visited.contains(current)) {
                throw new InternalException("Circular reference detected in the upper bound chain of type " + this);
            }
            visited.add(current);
            current = current.getUpperBound();
        }
        return current;
    }

    public Type getUltimateUpperBound() {
        var upperBound = getUpperBound();
        if(upperBound == this) {
            return this;
        }
        Set<Type> visited = new IdentitySet<>(List.of(this, upperBound));
        var last = upperBound;
        var current = upperBound.getUpperBound();
        while (last != current) {
            if(visited.contains(current)) {
                throw new InternalException("Circular reference detected in the upper bound chain of type " + this);
            }
            last = current;
            current = current.getUpperBound();
            visited.add(current);
        }
        return current;
    }

    public boolean isNullable() {
        return isAssignableFrom(NULL_TYPE);
    }

    public boolean isUnionNullable() {
        return isNullable() && this instanceof UnionType;
    }

    public boolean isNull() {
        return category.isNull();
    }

    public Type getConcreteType() {
        return this;
    }

    protected abstract boolean isAssignableFrom0(Type that);

    protected boolean isParameterized() {
        return false;
    }

    @Override
    public final boolean afterContextInitIds() {
        boolean b = afterContextInitIdsInternal();
        boolean p = isParameterized();
        if(p != isParameterized) {
            isParameterized = p;
            return true;
        }
        else {
            return b;
        }
    }

    protected boolean afterContextInitIdsInternal() {
        return false;
    }

    public boolean isAssignableFrom(Type that) {
        if (that instanceof NothingType) {
            return true;
        }
        var bound = that.getUpperBound();
        if (bound instanceof UnionType unionType) {
            return NncUtils.allMatch(unionType.getMembers(), this::isAssignableFrom);
        } else if (bound instanceof TypeIntersection intersection) {
            return NncUtils.anyMatch(intersection.getTypes(), this::isAssignableFrom);
        } else {
            return getLowerBound().isAssignableFrom0(bound);
        }
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    protected void setTemplate(boolean template) {
        isTemplate = template;
    }

    public boolean isUncertain() {
        return false;
    }

    public final boolean isWithinRange(Type that) {
        return getUpperBound().isAssignableFrom(that.getUpperBound()) &&
                that.getLowerBound().isAssignableFrom(getLowerBound());
    }

    public boolean isVariable() {
        return false;
    }

    public Set<TypeVariable> getVariables() {
        return Set.of();
    }

    public Type getLowerBound() {
        return this;
    }

    public Type getUpperBound() {
        return this;
    }

    public boolean isInstance(Instance value) {
        return isAssignableFrom(value.getType());
    }

    public boolean contains(Type that) {
        return this == that;
    }

    public boolean isInt() {
//        return (this instanceof PrimitiveType primType) && primType.getKind() == PrimitiveKind.INT;
        return isLong();
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
        return category.isEnum();
    }

    public boolean isInterface() {
        return category.isInterface();
    }

    public boolean isPojo() {
        return category.isPojo();
    }

    public boolean isVoid() {
        return category == TypeCategory.VOID;
    }

    public boolean isReference() {
        return isArray() || isPojo() ||
                (!(this instanceof ObjectType) && isNullable() && getUnderlyingType().isReference());
    }

//    public @Nullable ArrayType getArrayType() {
//        return arrayType;
//    }

//    public void setArrayType(@Nullable ArrayType arrayType) {
//        this.arrayType = arrayType;
//    }

//    @Nullable
//    public UnionType getNullableType() {
//        return nullableType;
//    }

    public Type getUnderlyingType() {
        if (this instanceof UnionType unionType) {
            if (isNullable()) {
                return NncUtils.findRequired(unionType.getMembers(), t -> !t.equals(NULL_TYPE));
            }
        }
        return this;
    }

//    public void setNullableType(@Nullable UnionType nullableType) {
//        this.nullableType = nullableType;
//    }

    public SQLType getSQLType() {
        return category.getSQLType();
    }

    public Set<ReferencePO> extractReferences(InstancePO instancePO) {
        return Set.of();
    }

    protected TypeDTO toDTO(TypeParam param, Long tmpId) {
        try (var context = SerializeContext.enter()) {
            return new TypeDTO(
                    getId(),
                    tmpId,
                    name,
                    code,
                    category.code(),
                    ephemeral,
                    anonymous,
                    param
            );
        }
    }

    public TypeDTO toDTO() {
        try (var context = SerializeContext.enter()) {
            return toDTO(getParam(), context.getTmpId(this));
        }
    }

    protected abstract TypeParam getParam();

    public Class<? extends Instance> getInstanceClass() {
        return Instance.class;
    }

    @Override
    public final List<Object> beforeRemove(IEntityContext context) {
        Set<Object> cascade = new IdentitySet<>(beforeRemoveInternal(context));
        if(isTemplate) {
            cascade.addAll(context.selectByKey(ClassType.TEMPLATE_IDX, this));
        }
//        else {
            cascade.addAll(
                    NncUtils.union(
                            context.selectByKey(FunctionType.PARAMETER_TYPE_KEY, this),
                            context.selectByKey(FunctionType.RETURN_TYPE_KEY, this)
                    )
            );
            cascade.addAll(context.selectByKey(UnionType.MEMBER_IDX, this));
            cascade.addAll(context.selectByKey(UncertainType.LOWER_BOUND_IDX, this));
            cascade.addAll(context.selectByKey(UncertainType.UPPER_BOUND_IDX, this));
            cascade.addAll(context.selectByKey(ArrayType.ELEMENT_TYPE_IDX, this));
            cascade.addAll(context.selectByKey(ClassType.TYPE_ARGUMENTS_IDX, this));
//        }
        return new ArrayList<>(cascade);
    }

    protected List<Object> beforeRemoveInternal(IEntityContext context) {
        return List.of();
    }

    public @Nullable Class<?> getNativeClass() {
        return category.getNativeClass();
    }

    @Override
    public String toString() {
        return "Type " + name + " (id: " + getId() + ")";
    }

    public abstract String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType);

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
