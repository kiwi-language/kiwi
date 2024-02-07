package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NamingUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EntityType("类型")
public abstract class Type extends Element implements LoadAware, GlobalKey {

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
    @SuppressWarnings("unused")
    private boolean templateFlag = false;
    //</editor-fold>

    @SuppressWarnings("FieldMayBeFinal") // for unit test
    private boolean dummyFlag = false;

    @Nullable
    private transient Closure<? extends Type> closure;
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
        return isAssignableFrom(NULL_TYPE);
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

    protected abstract boolean isAssignableFrom0(Type that);

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

    //<editor-fold desc="closure related">

    protected void onAncestorChanged0() {
    }

    private void addAncestorChangeListener(Runnable listener) {
        ancestorChangeListeners().add(listener);
    }

    private void removeAncestorChangeListener(Runnable listener) {
        ancestorChangeListeners().remove(listener);
    }

    protected final void onSuperTypesChanged() {
        if (superTypesCheckpoint != null) {
            for (Type oldSuperType : superTypesCheckpoint) {
                oldSuperType.removeAncestorChangeListener(this::onAncestorChanged);
            }
        }
        onAncestorChanged();
        resetSuperTypeListeners();
    }

    private void resetSuperTypeListeners() {
        var superTypes = getSuperTypes();
        for (Type superType : superTypes) {
            superType.addAncestorChangeListener(this::onAncestorChanged);
        }
        this.superTypesCheckpoint = new ArrayList<>(superTypes);
    }

    private void onAncestorChanged() {
        closure = null;
        onAncestorChanged0();
        ancestorChangeListeners().forEach(Runnable::run);
    }

    protected List<Runnable> ancestorChangeListeners() {
        if (ancestorChangeListeners == null)
            ancestorChangeListeners = new ArrayList<>();
        return ancestorChangeListeners;
    }

    private void ensureListenersInitialized() {
        if (superTypesCheckpoint == null) {
            for (Type superType : getSuperTypes())
                superType.ensureListenersInitialized();
            resetSuperTypeListeners();
        }
    }

    public Closure<? extends Type> getClosure() {
        ensureListenersInitialized();
        if (closure == null)
            closure = createClosure(getClosureElementJavaClass());
        return closure;
    }

    private <T extends Type> Closure<T> createClosure(Class<T> closureElementClass) {
        return new Closure<>(closureElementClass.cast(this), closureElementClass);
    }
    //</editor-fold>

    protected Class<? extends Type> getClosureElementJavaClass() {
        return Type.class;
    }

    @Override
    public final boolean afterContextInitIds() {
        return afterContextInitIdsInternal();
    }

    protected boolean afterContextInitIdsInternal() {
        return false;
    }

    public boolean isAssignableFrom(Type that) {
        if (that instanceof NeverType)
            return true;
        var bound = that.getUpperBound();
        return switch (bound) {
            case UnionType unionType -> NncUtils.allMatch(unionType.getMembers(), this::isAssignableFrom);
            case IntersectionType intersectionType ->
                    NncUtils.anyMatch(intersectionType.getTypes(), this::isAssignableFrom);
            default -> isAssignableFrom0(bound);
        };
    }

    public boolean isUncertain() {
        return false;
    }

    public final boolean contains(Type that) {
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

    protected TypeDTO toDTO(TypeParam param, Long tmpId) {
        try (var ignored = SerializeContext.enter()) {
            return new TypeDTO(
                    tryGetId(),
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
        try (var serContext = SerializeContext.enter()) {
            return toDTO(getParam(), serContext.getTmpId(this));
        }
    }

    protected abstract TypeParam getParam();

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
        cascade.addAll(context.selectByKey(ClassType.TYPE_ARGUMENTS_IDX, this));
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

}
