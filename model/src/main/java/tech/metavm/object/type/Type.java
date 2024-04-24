package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

@EntityType("类型")
public abstract class Type extends Element implements LoadAware, GlobalKey {

    public static final IndexDef<Type> IDX_ALL_FLAG = IndexDef.create(Type.class, "allFlag");

    public static final IndexDef<Type> IDX_CATEGORY = IndexDef.create(Type.class, "category");

    @SuppressWarnings("StaticInitializerReferencesSubClass")
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
    @EntityField("错误")
    private boolean error = false;

    //<editor-fold desc="search flags">

    private boolean allFlag = true;

    @SuppressWarnings("unused")
    private boolean templateFlag = false;
    //</editor-fold>

    @SuppressWarnings("FieldMayBeFinal") // for unit test
    private boolean dummyFlag = false;

    @Nullable
    private transient List<Type> superTypesCheckpoint;

    private transient List<Runnable> ancestorChangeListeners = new ArrayList<>();

    public Type(String name, @Nullable String code, boolean anonymous, boolean ephemeral, TypeCategory category) {
        this.name = name;
        this.code = NamingUtils.ensureValidTypeCode(code);
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.category = category;
    }

    @Override
    public void onLoad(IEntityContext context) {
    }

    public boolean isViewType(Type type) {
        return this == type;
    }

    protected void setTemplateFlag(boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    public String getName() {
        return name;
    }

    public String getTypeDesc() {
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
        this.code = NamingUtils.ensureValidTypeCode(code);
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

    public abstract TypeKey getTypeKey();

    @SuppressWarnings("unused")
    public boolean isDurable() {
        return !isEphemeral();
    }

    public boolean isCaptured() {
        return false;
    }

    public Set<CapturedType> getCapturedTypes() {
        var capturedTypes = new HashSet<CapturedType>();
        getCapturedTypes(capturedTypes);
        return capturedTypes;
    }

    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
    }

    public Type getCertainUpperBound() {
        if (!(this instanceof UncertainType)) {
            return this;
        }
        var current = getUpperBound();
        Set<Type> visited = new IdentitySet<>();
        while (current instanceof UncertainType) {
            if (visited.contains(current))
                throw new InternalException("Circular reference detected in the upper bound chain of type " + this);
            visited.add(current);
            current = current.getUpperBound();
        }
        return current;
    }

    public Type getUltimateUpperBound() {
        var upperBound = getUpperBound();
        if (upperBound == this) {
            return this;
        }
        Set<Type> visited = new IdentitySet<>(List.of(this, upperBound));
        var last = upperBound;
        var current = upperBound.getUpperBound();
        while (last != current) {
            if (visited.contains(current)) {
                throw new InternalException("Circular reference detected in the upper bound chain of type " + this);
            }
            last = current;
            current = current.getUpperBound();
            visited.add(current);
        }
        return current;
    }

    public boolean isNullable() {
        return isAssignableFrom(NULL_TYPE, null);
    }

    public boolean isBinaryNullable() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public Type getConcreteType() {
        return this;
    }

    protected abstract boolean isAssignableFrom0(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping);

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitType(this, s);
    }

    public Type transform(TypeTransformer<?> transformer) {
        return transformer.visitType(this, null);
    }

    public List<? extends Type> getSuperTypes() {
        return List.of();
    }

    public int getRank() {
        return 1;
    }

    @Override
    public final boolean afterContextInitIds() {
        return afterContextInitIdsInternal();
    }

    protected boolean afterContextInitIdsInternal() {
        return false;
    }

    public boolean isAssignableFrom(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        if (that instanceof NeverType)
            return true;
        return that instanceof ITypeVariable && isAssignableFrom1(that, typeMapping) || isAssignableFrom1(that.getUpperBound(), typeMapping);
    }

    private boolean isAssignableFrom1(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        return switch (that) {
            case UnionType unionType -> NncUtils.allMatch(unionType.getMembers(), that1 -> isAssignableFrom(that1, typeMapping));
            case IntersectionType intersectionType ->
                    NncUtils.anyMatch(intersectionType.getTypes(), that1 -> isAssignableFrom(that1, typeMapping));
            default -> isAssignableFrom0(that, typeMapping);
        };
    }

    public abstract boolean equals(Type that, @Nullable Map<TypeVariable, ? extends Type> mapping);

    public boolean isUncertain() {
        return false;
    }

    public final boolean contains(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        return getUpperBound().isAssignableFrom(that, typeMapping) && that.isAssignableFrom(getLowerBound(), typeMapping);
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
        return isAssignableFrom(value.getType(), null);
    }

    @NoProxy
    public boolean isLong() {
        return false;
    }

    @NoProxy
    public boolean isNumber() {
        return false;
    }

    @NoProxy
    public boolean isDouble() {
        return false;
    }

    @NoProxy
    public boolean isBoolean() {
        return false;
    }

    @NoProxy
    public boolean isString() {
        return false;
    }

    @NoProxy
    public boolean isTime() {
        return false;
    }

    @NoProxy
    public boolean isPassword() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    @NoProxy
    public boolean isArray() {
        return false;
    }

    @NoProxy
    public boolean isPrimitive() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isPojo() {
        return false;
    }

    @NoProxy
    public boolean isVoid() {
        return false;
    }

    public boolean isReference() {
        return isArray() || isPojo() || (isBinaryNullable() && getUnderlyingType().isReference());
    }

    public Type getUnderlyingType() {
        return this;
    }

    public ColumnKind getSQLType() {
        return category.getSQLType();
    }

    protected TypeDTO toDTO(TypeParam param) {
        try (var ser = SerializeContext.enter()) {
            return new TypeDTO(
                    ser.getId(this),
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
        try (var serContext = SerializeContext.enter()) {
            return toDTO(getParam(serContext));
        }
    }

    protected abstract TypeParam getParam(SerializeContext serializeContext);

    @Override
    public final List<Object> beforeRemove(IEntityContext context) {
        Set<Object> cascade = new IdentitySet<>(beforeRemoveInternal(context));
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
        cascade.addAll(context.selectByKey(Klass.TYPE_ARGUMENTS_IDX, this));
        return new ArrayList<>(cascade);
    }

    protected List<?> beforeRemoveInternal(IEntityContext context) {
        return List.of();
    }

    @Override
    protected String toString0() {
        return "Type " + name + " (id: " + tryGetId() + ")";
    }

    @Override
    public abstract String getGlobalKey(@NotNull BuildKeyContext context);

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public abstract String getInternalName(@org.jetbrains.annotations.Nullable Flow current);

    public TypeTag getTag() {
        return TypeTag.fromCategory(category);
    }

    public TypeId getTypeId() {
        return new TypeId(TypeTag.fromCategory(category), getId().getPhysicalId());
    }

    public abstract Type copy();

    public String toTypeExpression() {
        try(var serContext = SerializeContext.enter()) {
            return toTypeExpression(serContext);
        }
    }

    public abstract String toTypeExpression(SerializeContext serializeContext);

    public  void write(InstanceOutput output) {
        output.write(category.code());
        write0(output);
    }

    public abstract void write0(InstanceOutput output);

    public static Type readType(InstanceInput input, TypeDefProvider typeDefProvider) {
        var category = TypeCategory.fromCode(input.read());
        return switch (category) {
            case CLASS, INTERFACE, ENUM, VALUE -> ClassType.read(input, typeDefProvider);
            case VARIABLE -> VariableType.read(input, typeDefProvider);
            case CAPTURED -> CapturedType.read(input, typeDefProvider);
            case LONG, DOUBLE, NULL, VOID, TIME, PASSWORD, STRING, BOOLEAN -> PrimitiveType.read(input);
            case FUNCTION -> FunctionType.read(input, typeDefProvider);
            case UNCERTAIN -> UncertainType.read(input, typeDefProvider);
            case UNION ->  UnionType.read(input, typeDefProvider);
            case INTERSECTION -> IntersectionType.read(input, typeDefProvider);
            case READ_ONLY_ARRAY -> ArrayType.read(input, ArrayKind.READ_ONLY, typeDefProvider);
            case READ_WRITE_ARRAY -> ArrayType.read(input, ArrayKind.READ_WRITE, typeDefProvider);
            case CHILD_ARRAY -> ArrayType.read(input, ArrayKind.CHILD, typeDefProvider);
            case NOTHING -> new NeverType();
            case OBJECT -> new AnyType();
        };
    }

}
