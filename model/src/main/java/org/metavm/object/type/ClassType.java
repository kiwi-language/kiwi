package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.expression.Var;
import org.metavm.flow.Flow;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TypeId;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Entity
public class ClassType extends CompositeType implements ISubstitutor, GenericDeclarationRef {

    public static ClassType create(Klass klass, List<? extends Type> typeArguments) {
        assert klass.getDeclaringKlass() == null;
        return create(null, klass, typeArguments);
    }

    public static ClassType create(@Nullable GenericDeclarationRef owner, Klass klass, List<? extends Type> typeArguments) {
        if (typeArguments.equals(klass.getDefaultTypeArguments()))
            typeArguments = List.of();
        if(klass.getEnclosingFlow() != null && owner == null)
            throw new IllegalArgumentException("Missing owner for local class type " + klass);
        return new ClassType(owner, klass, typeArguments);
    }

    public static final Logger logger = LoggerFactory.getLogger(ClassType.class);

    private final @Nullable GenericDeclarationRef owner;
    private final Klass klass;
    private final @Nullable ValueArray<Type> typeArguments;
    @CopyIgnore
    private transient TypeSubstitutor substitutor;
    @CopyIgnore
    private transient TypeMetadata typeMetadata;

    public ClassType(@Nullable GenericDeclarationRef owner, @NotNull Klass klass, List<? extends Type> typeArguments) {
        assert klass.getEnclosingFlow() == null && klass.getDeclaringKlass() == null || owner != null :
            "owner is missing for local or inner class type " + klass.getName();
        this.owner = owner;
        this.klass = klass;
        this.typeArguments = typeArguments.isEmpty() ? null : new ValueArray<>(Type.class, typeArguments);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return owner == null && typeArguments == null ?
                (getTypeTag() > 0 ?
                        new TaggedClassTypeKey(getTypeDefId.apply(klass), getTypeTag()) :
                        new ClassTypeKey(getTypeDefId.apply(klass))
                ) :
                new ParameterizedTypeKey(
                        owner != null ? owner.toGenericDeclarationKey(getTypeDefId) : null,
                        klass.getId(), NncUtils.map(typeArguments, type -> type.toTypeKey(getTypeDefId)));
    }

    @Override
    public GenericDeclarationRefKey toGenericDeclarationKey(Function<ITypeDef, Id> getTypeDefId) {
        return (GenericDeclarationRefKey) toTypeKey(getTypeDefId);
    }

    public Klass getKlass() {
        return klass;
    }

    public boolean isStruct() {
        return klass.isStruct();
    }

    public ClassType getTemplateType() {
        return klass.getType();
    }

    public Type getFirstTypeArgument() {
        return getTypeArguments().get(0);
    }

    public @Nullable ClassType findAncestor(Predicate<ClassType> filter) {
        if(filter.test(this))
            return this;
        var superType = getSuperType();
        ClassType t;
        if(superType != null && (t = superType.findAncestor(filter)) != null)
            return t;
        for (ClassType it : getInterfaces()) {
            if((t = it.findAncestor(filter)) != null)
                return t;
        }
        return null;
    }

    public @Nullable ClassType findAncestorByKlass(Klass rawKlass) {
        return findAncestor(a -> a.getKlass() == rawKlass);
    }

    public List<Type> getTypeArguments() {
        // the type arguments should be the list of type parameters for a raw ClassType
        return typeArguments != null ? typeArguments.toList() : klass.getDefaultTypeArguments();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof ClassType thatClassType) {
            if (klass == thatClassType.klass) {
                if(owner instanceof ClassType ownerType && thatClassType.owner instanceof ClassType thatOwnerType) {
                    if(!ownerType.isAssignableFrom(thatOwnerType))
                        return false;
                } else if (!Objects.equals(owner, thatClassType.owner))
                    return false;
                if (typeArguments == null && thatClassType.typeArguments == null)
                    return true;
                else if(typeArguments != null && thatClassType.typeArguments != null)
                    return NncUtils.biAllMatch(typeArguments, thatClassType.typeArguments, Type::contains);
                else
                    return false;
            } else {
                var thatSuper = thatClassType.getSuperType();
                if (thatSuper != null && isAssignableFrom(thatSuper))
                    return true;
                if (isInterface()) {
                    for (ClassType thatInterface : thatClassType.getInterfaces()) {
                        if (isAssignableFrom(thatInterface))
                            return true;
                    }
                }
                return false;
            }
        }
        else
            return false;
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitClassType(this, s);
    }

    public boolean isInterface() {
        return klass.isInterface();
    }

    public @Nullable ClassType getSuperType() {
        var si = klass.getSuperTypeIndex();
        return si != null ? getTypeMetadata().getClasType(si) : null;
    }

    public List<ClassType> getInterfaces() {
        return NncUtils.map(klass.getInterfaceIndexes(), idx -> getTypeMetadata().getClasType(idx));
    }

    @Override
    public Type substitute(Type type) {
        if (typeArguments != null) {
            if (substitutor == null)
                substitutor = new TypeSubstitutor(NncUtils.map(klass.getTypeParameters(), TypeVariable::getType), typeArguments.toList());
            return type.accept(substitutor);
        } else
            return type;
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof ClassType that && Objects.equals(owner, that.owner) && klass == that.klass &&
                Objects.equals(typeArguments, that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, klass, typeArguments);
    }

    @Override
    public boolean isEnum() {
        return klass.isEnum();
    }

    @Override
    public String getName() {
        return klass.getName();
    }

    @Override
    public TypeCategory getCategory() {
        return klass.getKind().typeCategory();
    }

    @Override
    public boolean isEphemeral() {
        return klass.isEphemeral();
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if (isParameterized())
            return klass.getQualifiedName() + "<" + NncUtils.join(typeArguments, type -> type.getInternalName(current)) + ">";
        else
            return klass.getQualifiedName();

    }

    @Override
    public List<Type> getComponentTypes() {
        return getTypeArguments();
    }

    @Override
    public boolean isCaptured() {
        return typeArguments != null && NncUtils.anyMatch(typeArguments, Type::isCaptured);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var id = getTypeDefExpr == null ? Constants.ID_PREFIX + serializeContext.getStringId(klass) : getTypeDefExpr.apply(klass);
        var tag = getTypeTag();
        return typeArguments == null ? (tag == 0 ? id : id + ":" + tag)
                : id + "<" + NncUtils.join(typeArguments, type -> type.toExpression(serializeContext, getTypeDefExpr)) + ">";
    }

    @Override
    public int getTypeKeyCode() {
        return typeArguments == null ? (getTypeTag() == 0 ? WireTypes.CLASS_TYPE : WireTypes.TAGGED_CLASS_TYPE) : WireTypes.PARAMETERIZED_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        if (owner == null && typeArguments == null) {
            var tag = getTypeTag();
            if (tag == 0) {
                output.write(WireTypes.CLASS_TYPE);
                output.writeEntityId(klass);
            } else {
                output.write(WireTypes.TAGGED_CLASS_TYPE);
                output.writeEntityId(klass);
                output.writeLong(tag);
            }
        } else {
            output.write(WireTypes.PARAMETERIZED_TYPE);
            if(owner != null)
                owner.write(output);
            else
                output.write(WireTypes.NULL);
            output.writeEntityId(klass);
            if(typeArguments == null)
                output.writeInt(0);
            else {
                output.writeInt(typeArguments.size());
                typeArguments.forEach(t -> t.write(output));
            }
        }
    }

    public static ClassType read(MvInput input) {
        return new ClassType(null, input.getKlass(input.readId()), List.of());
    }

    public static ClassType readTagged(MvInput input) {
        var type = new ClassType(null, input.getKlass(input.readId()), List.of());
        input.readLong();
        return type;
    }

    public static ClassType readParameterized(MvInput input) {
        var owner = (GenericDeclarationRef) input.readGenericDeclarationRef();
        var klass = input.getKlass(input.readId());
        int numTypeArgs = input.readInt();
        var typeArgs = new ArrayList<Type>(numTypeArgs);
        for (int i = 0; i < numTypeArgs; i++)
            typeArgs.add(Type.readType(input));
        return new ClassType(owner, klass, typeArgs);
    }

    public boolean isParameterized() {
        return typeArguments != null;
    }

    public boolean isList() {
        return klass.isList();
    }

    public boolean isChildList() {
        return klass.isChildList();
    }

    @Override
    public <S> void acceptComponents(TypeVisitor<?, S> visitor, S s) {
        getTypeArguments().forEach(t -> t.accept(visitor, s));
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        if(owner != null)
            owner.forEachTypeDef(action);
        action.accept(klass);
        getTypeArguments().forEach(t -> t.forEachTypeDef(action));
    }

    @Override
    public String getTypeDesc() {
        String prefix;
        if(owner != null)
            prefix =  owner.getTypeDesc() + "." + klass.getName();
        else
            prefix = klass.getTypeDesc();
        return prefix + (typeArguments != null ? "<" + NncUtils.join(typeArguments, Type::getTypeDesc) + ">" : "");
    }

    @Override
    public TypeId getTypeId() {
        return new TypeId(TypeTag.fromCategory(getCategory()), getKlass().getId().getTreeId());
    }

    public int getTypeTag() {
        return typeArguments == null && klass.getTag() < 1000000 ? (int) klass.getTag() : 0;
    }

    @Override
    public boolean isValue() {
        return klass.getKind() == ClassKind.VALUE;
    }

    public boolean isAbstract() {
        return klass.isAbstract();
    }

    public boolean isKlassNull() {
        return klass == null;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Nullable
    public GenericDeclarationRef getOwner() {
        return owner;
    }

    public TypeMetadata getTypeMetadata() {
        if (typeMetadata == null) {
//            logger.debug("Getting type metadata for type {}, owner: {}", this, owner);
            typeMetadata = klass.getTypeMetadata(getAllTypeArguments());
        }
        return typeMetadata;
    }

    public @Nullable MethodRef tryResolveMethod(String name, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly) {
        var candidates = new ArrayList<MethodRef>();
        getCallCandidates(name, argumentTypes, typeArguments, staticOnly, candidates);
        out:
        for (var m1 : candidates) {
            for (var m2 : candidates) {
                if (m1 != m2 && m1.isHiddenBy(m2))
                    continue out;
            }
            return m1;
        }
        return null;
    }

    private void getCallCandidates(String name,
                                   List<Type> argumentTypes,
                                   List<Type> typeArguments,
                                   boolean staticOnly,
                                   List<MethodRef> candidates) {
        klass.getMethods().forEach(m -> {
            if ((m.isStatic() || !staticOnly) && name.equals(m.getName()) && m.getParameters().size() == argumentTypes.size()) {
                if (NncUtils.isNotEmpty(typeArguments)) {
                    if (m.getTypeParameters().size() == typeArguments.size()) {
                        var pMethod = new MethodRef(this, m, typeArguments);
                        if (pMethod.matches(name, argumentTypes))
                            candidates.add(pMethod);
                    }
                } else {
                    var ref = new MethodRef(this, m, List.of());
                    if (ref.matches(name, argumentTypes))
                        candidates.add(ref);
                }
            }
        });
        forEachSuper(k -> k.getCallCandidates(name, argumentTypes, typeArguments, staticOnly, candidates));
    }

    void forEachSuper(Consumer<ClassType> action) {
        klass.forEachSuperType(action, getTypeMetadata());
    }

    public MethodRef resolveMethod(String name, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly) {
        var found = tryResolveMethod(name, argumentTypes, typeArguments, staticOnly);
        if (found != null)
            return found;
//        if (DebugEnv.resolveVerbose) {
//            logger.info("method resolution failed");
//            forEachMethod(m -> logger.info(m.getSignatureString()));
//        }
//        foreachMethod(m -> logger.debug("Method: {}", m));
        throw new NullPointerException(
                String.format("Can not find method %s%s(%s) in type %s",
                        NncUtils.isNotEmpty(typeArguments) ? "<" + NncUtils.join(typeArguments, Type::getName) + ">" : "",
                        name,
                        NncUtils.join(argumentTypes, Type::getName, ","), getName()));
    }

    @Nullable
    public MethodRef tryResolveNonParameterizedMethod(MethodRef methodRef) {
        NncUtils.requireFalse(methodRef.isParameterized());
        return klass.findOverride(methodRef.getRawFlow(), getTypeMetadata());
    }

    public List<MethodRef> getMethods() {
        return NncUtils.map(klass.getMethods(), m -> new MethodRef(this, m, List.of()));
    }

    public void foreachSelfMethod(Consumer<MethodRef> action) {
        for (Method method : klass.getMethods()) {
            action.accept(new MethodRef(this, method, List.of()));
        }
    }

    public void foreachSuperClass(Consumer<ClassType> action) {
        action.accept(this);
        var superIdx = klass.getSuperTypeIndex();
        if(superIdx != null)
            getTypeMetadata().getClasType(superIdx).foreachSuperClass(action);
    }

    public void foreachSuperClassTopDown(Consumer<ClassType> action) {
        var superIdx = klass.getSuperTypeIndex();
        if(superIdx != null)
            getTypeMetadata().getClasType(superIdx).foreachSuperClassTopDown(action);
        action.accept(this);
    }

    public void foreachAncestor(Consumer<ClassType> action) {
        var queue = new LinkedList<ClassType>();
        queue.offer(this);
        while (!queue.isEmpty()) {
            var t = queue.poll();
            action.accept(t);
            t.forEachSuper(queue::offer);
        }
    }

    public void foreachMethod(Consumer<MethodRef> action) {
        for (Method method : klass.getMethods()) {
            action.accept(new MethodRef(this, method, List.of()));
        }
        forEachSuper(s -> s.foreachMethod(action));
    }

    public MethodRef getOverride(MethodRef methodRef) {
        return Objects.requireNonNull(findOverride(methodRef));
    }

    public @Nullable MethodRef findOverride(@NotNull MethodRef methodRef) {
        if (methodRef.getDeclaringType().equals(this))
            return methodRef;
        var override = klass.findOverride(methodRef.getRawFlow(), getTypeMetadata());
        if (override != null && methodRef.isParameterized())
            override = new MethodRef(override.getDeclaringType(), override.getRawFlow(), methodRef.getTypeArguments());
        return override;
    }

    public void forEachField(Consumer<FieldRef> action) {
        var superIdx = klass.getSuperTypeIndex();
        if(superIdx != null)
            getTypeMetadata().getClasType(superIdx).forEachField(action);
        for (Field field : klass.getFields()) {
            action.accept(new FieldRef(this ,field));
        }
    }

    public MethodRef getMethod(Predicate<MethodRef> filter) {
        return Objects.requireNonNull(findMethod(filter), () -> "Cannot find method with the given filter in type " + this);
    }

    public String getQualifiedName() {
        return klass.getQualifiedName();
    }

    public boolean isLocal() {
        return klass.isLocal();
    }

    public MethodRef getMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var found = findMethodByNameAndParamTypes(name, parameterTypes);
        if(found == null) {
            throw new NullPointerException(String.format("Can not find method %s(%s) in klass %s",
                    name, NncUtils.join(parameterTypes, Type::getTypeDesc, ","), getTypeDesc()));
        }
        return found;
    }

    public MethodRef findMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var method = NncUtils.find(getMethods(),
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        var superType = getSuperType();
        if (superType != null) {
            var m = superType.findMethodByNameAndParamTypes(name, parameterTypes);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : getInterfaces()) {
                var m = it.findMethodByNameAndParamTypes(name, parameterTypes);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    private boolean isEffectiveAbstract() {
        return klass.isEffectiveAbstract();
    }

    public MethodRef getSingleAbstractMethod() {
        return new MethodRef(this, klass.getSingleAbstractMethod(), List.of());
    }

    public MethodRef getMethod(Method method) {
        return Objects.requireNonNull(findMethod(method), () -> "Cannot find method " + method + " in type " + this);
    }

    public MethodRef findMethod(Method method) {
        if(method.getDeclaringType() == klass)
            return new MethodRef(this, method, List.of());
        MethodRef found;
        var superType = getSuperType();
        if(superType != null && (found = superType.findMethod(method)) != null)
            return found;
        for (ClassType it : getInterfaces()) {
            if((found = it.findMethod(method)) != null)
                return found;
        }
        return null;
    }

    public MethodRef findSelfMethod(Predicate<MethodRef> filter) {
        for (Method method : klass.getMethods()) {
            var r = new MethodRef(this, method, List.of());
            if(filter.test(r))
                return r;
        }
        return null;
    }

    public MethodRef findMethod(Predicate<MethodRef> filter) {
        MethodRef found = findSelfMethod(filter);
        if(found != null)
            return found;
        var superType = getSuperType();
        if(superType != null && (found = superType.findMethod(filter)) != null)
            return found;
        for (ClassType it : getInterfaces()) {
            if((found = it.findMethod(filter)) != null)
                return found;
        }
        return null;
    }

    public void foreachField(Consumer<FieldRef> action) {
        for (Field field : klass.getFields()) {
            action.accept(new FieldRef(this, field));
        }
        var superType = getSuperType();
        if (superType != null)
            superType.foreachField(action);
    }

    public @Nullable FieldRef findField(Predicate<FieldRef> filter) {
        for (Field field : klass.getFields()) {
            var fr = new FieldRef(this, field);
            if(filter.test(fr))
                return fr;
        }
        var superType = getSuperType();
        return superType != null ? superType.findField(filter) : null;
    }

    public FieldRef getFieldByName(String fieldName) {
        return Objects.requireNonNull(findField(f -> f.getName().equals(fieldName)),
                () -> "Cannot find field with name " + fieldName + " in klass " + klass.getQualifiedName());
    }

    public boolean isTemplate() {
        return klass.isTemplate() && !isParameterized();
    }

    public PropertyRef getPropertyByVar(Var v) {
        var p = klass.getPropertyByVar(v);
        return switch (p) {
            case Field f -> new FieldRef(this, f);
            case Method m -> new MethodRef(this, m, List.of());
            default -> throw new IllegalStateException("Unrecognized property: " + p);
        };
    }

    public FieldRef findFieldByName(String name) {
        var field = klass.findFieldByName(name);
        return field != null ? new FieldRef(this, field) : null;
    }

    public FieldRef getField(Field field) {
        if(field.getDeclaringType() == klass)
            return new FieldRef(this, field);
        var superType = getSuperType();
        if (superType != null)
            return superType.getField(field);
        throw new NullPointerException("Cannot find field " + field.getName() + " in type " + this);
    }

    public long getKlassTag() {
        return klass.getTag();
    }

    public MethodRef getReadObjectMethod() {
        var method = klass.getReadObjectMethod();
        return method != null ? new MethodRef(this, method, List.of()) : null;
    }

    public MethodRef getWriteObjectMethod() {
        var method = klass.getWriteObjectMethod();
        return method != null ? new MethodRef(this, method, List.of()) : null;
    }

    public MethodRef getMethodByName(String name) {
        return getMethod(m -> m.getName().equals(name));
    }

    public void foreachIndex(Consumer<IndexRef> action) {
        var superType = getSuperType();
        if (superType != null)
            superType.foreachIndex(action);
        for (Constraint constraint : klass.getConstraints()) {
            if (constraint instanceof Index index) {
                action.accept(new IndexRef(this ,index));
            }
        }
    }

    public IndexRef findSelfIndex(Predicate<IndexRef> filter) {
        for (Constraint constraint : klass.getConstraints()) {
            if (constraint instanceof Index index) {
                var indexRef = new IndexRef(this, index);
                if (filter.test(indexRef))
                    return indexRef;
            }
        }
        return null;
    }

    public MethodRef findSetterByPropertyName(String name) {
        return findMethod(m -> m.isSetter() && m.getPropertyName().equals(name));
    }

    public MethodRef findGetterByPropertyName(String name) {
        return findMethod(m -> m.isGetter() && m.getPropertyName().equals(name));
    }
}
