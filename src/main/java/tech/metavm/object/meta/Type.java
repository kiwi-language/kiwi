package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.object.meta.rest.dto.TypeKey;
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
public abstract class Type extends Element implements LoadAware, GlobalKey {

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


    @Nullable
    private transient Closure<? extends Type> closure;
    @Nullable
    private transient List<Type> superTypesCheckpoint;

    private transient List<Runnable> ancestorChangeListeners = new ArrayList<>();

    public Type(String name, boolean anonymous, boolean ephemeral, TypeCategory category) {
        this.name = name;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.category = category;
    }

    @Override
    public void onLoad() {
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

    public abstract TypeKey getTypeKey();

    @SuppressWarnings("unused")
    public boolean isPersistent() {
        return !isEphemeral();
    }

    public Type getCertainUpperBound() {
        if (!(this instanceof UncertainType)) {
            return this;
        }
        var current = getUpperBound();
        Set<Type> visited = new IdentitySet<>();
        while (current instanceof UncertainType) {
            if (visited.contains(current)) {
                throw new InternalException("Circular reference detected in the upper bound chain of type " + this);
            }
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
        if(ancestorChangeListeners == null)
            ancestorChangeListeners = new ArrayList<>();
        return ancestorChangeListeners;
    }

    private void ensureListenersInitialized() {
        if(superTypesCheckpoint == null) {
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
        if (that instanceof NothingType) {
            return true;
        }
        var bound = that.getUpperBound();
        if (bound instanceof UnionType unionType) {
            return NncUtils.allMatch(unionType.getMembers(), this::isAssignableFrom);
        } else if (bound instanceof IntersectionType intersection) {
            return NncUtils.anyMatch(intersection.getTypes(), this::isAssignableFrom);
        } else {
            return getLowerBound().isAssignableFrom0(bound);
        }
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

    public Type getUnderlyingType() {
        if (this instanceof UnionType unionType) {
            if (isNullable()) {
                return NncUtils.findRequired(unionType.getMembers(), t -> !t.equals(NULL_TYPE));
            }
        }
        return this;
    }

    public SQLType getSQLType() {
        return category.getSQLType();
    }

    public Set<ReferencePO> extractReferences(InstancePO instancePO) {
        return Set.of();
    }

    protected TypeDTO toDTO(TypeParam param, Long tmpId) {
        try (var ignored = SerializeContext.enter()) {
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
        return "Type " + name + " (id: " + getId() + ")";
    }

    @Override
    public abstract String getKey(Function<Type, java.lang.reflect.Type> getJavaType);

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

}
