package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.expression.Var;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.TypeId;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import java.util.LinkedList;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Entity
@Slf4j
public abstract class ClassType extends CompositeType implements GenericDeclarationRef {

    @Override
    public GenericDeclarationRefKey toGenericDeclarationKey(Function<ITypeDef, Id> getTypeDefId) {
        return (GenericDeclarationRefKey) toTypeKey(getTypeDefId);
    }

    public abstract Klass getKlass();

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof ClassType thatClassType) {
            if (getKlass() == thatClassType.getKlass()) {
                if (getOwner() instanceof ClassType ownerType && thatClassType.getOwner() instanceof ClassType thatOwnerType) {
                    if (!ownerType.isAssignableFrom(thatOwnerType))
                        return false;
                } else if (!Objects.equals(getOwner(), thatClassType.getOwner()))
                    return false;
                return Utils.biAllMatch(getTypeArguments(), thatClassType.getTypeArguments(), Type::contains);
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
        } else
            return false;
    }

    public boolean isStruct() {
        return getKlass().isStruct();
    }

    public ClassType getTemplateType() {
        return getKlass().getType();
    }

    public Type getFirstTypeArgument() {
        return getTypeArguments().getFirst();
    }

    public @Nullable ClassType findSuper(Predicate<ClassType> filter) {
        if (filter.test(this))
            return this;
        var superType = getSuperType();
        ClassType t;
        if (superType != null && (t = superType.findSuper(filter)) != null)
            return t;
        for (ClassType it : getInterfaces()) {
            if ((t = it.findSuper(filter)) != null)
                return t;
        }
        return null;
    }

    public @Nullable ClassType asSuper(Klass klass) {
        return findSuper(a -> a.getKlass() == klass);
    }

    public List<Type> getTypeArguments() {
        return List.of();
    }

    public boolean isInterface() {
        return getKlass().isInterface();
    }

    public @Nullable ClassType getSuperType() {
        var si = getKlass().getSuperTypeIndex();
        return si != null ? getTypeMetadata().getClasType(si) : null;
    }

    public List<ClassType> getInterfaces() {
        return Utils.map(getKlass().getInterfaceIndexes(), idx -> getTypeMetadata().getClasType(idx));
    }

    @Override
    public boolean isEnum() {
        return getKlass().isEnum();
    }

    @Override
    public String getName() {
        return getKlass().getName();
    }

    @Override
    public TypeCategory getCategory() {
        return getKlass().getKind().typeCategory();
    }

    @Override
    public boolean isEphemeral() {
        return getKlass().isEphemeralKlass();
    }

    @Override
    public List<Type> getComponentTypes() {
        return getTypeArguments();
    }

    @Override
    public <S> void acceptComponents(TypeVisitor<?, S> visitor, S s) {
        getTypeArguments().forEach(t -> t.accept(visitor, s));
    }

    @Override
    public TypeId getTypeId() {
        return new TypeId(TypeTag.fromCategory(getCategory()), getKlass().getId().getTreeId());
    }

    @Override
    public boolean isValueType() {
        return getKlass().getKind() == ClassKind.VALUE;
    }

    public boolean isAbstract() {
        return getKlass().isAbstract();
    }

    public boolean isKlassNull() {
        return getKlass() == null;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    public TypeMetadata getTypeMetadata() {
        return getKlass().getConstantPool();
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
        getKlass().getMethods().forEach(m -> {
            if ((m.isStatic() || !staticOnly) && name.equals(m.getName()) && m.getParameters().size() == argumentTypes.size()) {
                if (Utils.isNotEmpty(typeArguments)) {
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
        getKlass().forEachSuperType(action, getTypeMetadata());
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
                        Utils.isNotEmpty(typeArguments) ? "<" + Utils.join(typeArguments, Type::getName) + ">" : "",
                        name,
                        Utils.join(argumentTypes, Type::getName, ","), getName()));
    }

    @Nullable
    public MethodRef tryResolveNonParameterizedMethod(MethodRef methodRef) {
        Utils.require(!methodRef.isParameterized());
        return getKlass().findOverride(methodRef.getRawFlow(), getTypeMetadata());
    }

    public List<MethodRef> getMethods() {
        return Utils.map(getKlass().getMethods(), m -> new MethodRef(this, m, List.of()));
    }

    public MethodRef getConstructor() {
        for (Method method : getKlass().getMethods()) {
            if (method.isConstructor())
                return new MethodRef(this, method, List.of());
        }
        throw new IllegalStateException("Missing constructor in " + getTypeDesc());
    }

    public List<ClassType> getInnerClassTypes() {
        return Utils.map(getKlass().getKlasses(), k -> KlassType.create(
                this, k, k.getDefaultTypeArguments()
        ));
    }

    public void foreachSelfMethod(Consumer<MethodRef> action) {
        for (Method method : getKlass().getMethods()) {
            action.accept(new MethodRef(this, method, List.of()));
        }
    }

    public void foreachSuperClass(Consumer<ClassType> action) {
        action.accept(this);
        var superIdx = getKlass().getSuperTypeIndex();
        if (superIdx != null)
            getTypeMetadata().getClasType(superIdx).foreachSuperClass(action);
    }

    public void foreachSuperClassTopDown(Consumer<ClassType> action) {
        var superIdx = getKlass().getSuperTypeIndex();
        if (superIdx != null)
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
        for (Method method : getKlass().getMethods()) {
            action.accept(new MethodRef(this, method, List.of()));
        }
        forEachSuper(s -> s.foreachMethod(action));
    }

    public MethodRef getOverride(MethodRef methodRef) {
        var override = findOverride(methodRef);
        return Objects.requireNonNull(override, () -> "Cannot find override for method " + methodRef + " in klass " + getKlass().getTypeDesc());
    }

    public @Nullable MethodRef findOverride(@NotNull MethodRef methodRef) {
        if (methodRef.getDeclaringType().equals(this))
            return methodRef;
        var override = getKlass().findOverride(methodRef.getRawFlow(), getTypeMetadata());
        if (override != null && methodRef.isParameterized())
            override = new MethodRef(override.getDeclaringType(), override.getRawFlow(), methodRef.getTypeArguments());
        return override;
    }

    public void forEachField(Consumer<FieldRef> action) {
        var superIdx = getKlass().getSuperTypeIndex();
        if (superIdx != null)
            getTypeMetadata().getClasType(superIdx).forEachField(action);
        for (Field field : getKlass().getFields()) {
            action.accept(new FieldRef(this, field));
        }
    }

    public MethodRef getMethod(Predicate<MethodRef> filter) {
        return Objects.requireNonNull(findMethod(filter), () -> "Cannot find method with the given filter in type " + this);
    }

    public String getQualifiedName() {
        return getKlass().getQualifiedName();
    }

    public boolean isLocal() {
        return getKlass().isLocal();
    }

    public MethodRef getMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var found = findMethodByNameAndParamTypes(name, parameterTypes);
        if (found == null) {
            throw new NullPointerException(String.format("Can not find method %s(%s) in klass %s",
                    name, Utils.join(parameterTypes, Type::getTypeDesc, ","), getTypeDesc()));
        }
        return found;
    }

    public MethodRef findMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var method = Utils.find(getMethods(),
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
        return getKlass().isEffectiveAbstract();
    }

    public MethodRef getSingleAbstractMethod() {
        return new MethodRef(this, getKlass().getSingleAbstractMethod(), List.of());
    }

    public MethodRef getMethod(Method method) {
        return Objects.requireNonNull(findMethod(method), () -> "Cannot find method " + method + " in type " + this);
    }

    public MethodRef findMethod(Method method) {
        if (method.getDeclaringType() == getKlass())
            return new MethodRef(this, method, List.of());
        MethodRef found;
        var superType = getSuperType();
        if (superType != null && (found = superType.findMethod(method)) != null)
            return found;
        for (ClassType it : getInterfaces()) {
            if ((found = it.findMethod(method)) != null)
                return found;
        }
        return null;
    }

    public MethodRef findSelfMethod(Predicate<MethodRef> filter) {
        for (Method method : getKlass().getMethods()) {
            var r = new MethodRef(this, method, List.of());
            if (filter.test(r))
                return r;
        }
        return null;
    }

    public MethodRef findMethod(Predicate<MethodRef> filter) {
        MethodRef found = findSelfMethod(filter);
        if (found != null)
            return found;
        var superType = getSuperType();
        if (superType != null && (found = superType.findMethod(filter)) != null)
            return found;
        for (ClassType it : getInterfaces()) {
            if ((found = it.findMethod(filter)) != null)
                return found;
        }
        return null;
    }

    public void foreachField(Consumer<FieldRef> action) {
        for (Field field : getKlass().getFields()) {
            action.accept(new FieldRef(this, field));
        }
        var superType = getSuperType();
        if (superType != null)
            superType.foreachField(action);
    }

    public @Nullable FieldRef findField(Predicate<FieldRef> filter) {
        for (Field field : getKlass().getFields()) {
            var fr = new FieldRef(this, field);
            if (filter.test(fr))
                return fr;
        }
        var superType = getSuperType();
        return superType != null ? superType.findField(filter) : null;
    }

    public FieldRef getFieldByName(String fieldName) {
        return Objects.requireNonNull(findField(f -> f.getName().equals(fieldName)),
                () -> "Cannot find field with name " + fieldName + " in klass " + getKlass().getQualifiedName());
    }

    public boolean isParameterized() {
        return false;
    }

    public boolean isTemplate() {
        return getKlass().isTemplate() && !isParameterized();
    }

    public PropertyRef getPropertyByVar(Var v) {
        var p = getKlass().getPropertyByVar(v);
        return switch (p) {
            case Field f -> new FieldRef(this, f);
            case Method m -> new MethodRef(this, m, List.of());
            default -> throw new IllegalStateException("Unrecognized property: " + p);
        };
    }

    public FieldRef findFieldByName(String name) {
        var field = getKlass().findFieldByName(name);
        return field != null ? new FieldRef(this, field) : null;
    }

    public FieldRef getField(Field field) {
        if (field.getDeclaringType() == getKlass())
            return new FieldRef(this, field);
        var superType = getSuperType();
        if (superType != null)
            return superType.getField(field);
        throw new NullPointerException("Cannot find field " + field.getName() + " in type " + this);
    }

    public long getKlassTag() {
        return getKlass().getTag();
    }

    public MethodRef getReadObjectMethod() {
        var method = getKlass().getReadObjectMethod();
        return method != null ? new MethodRef(this, method, List.of()) : null;
    }

    public MethodRef getWriteObjectMethod() {
        var method = getKlass().getWriteObjectMethod();
        return method != null ? new MethodRef(this, method, List.of()) : null;
    }

    public MethodRef getMethodByName(String name) {
        return getMethod(m -> m.getName().equals(name));
    }

    public void foreachIndex(Consumer<IndexRef> action) {
        var superType = getSuperType();
        if (superType != null)
            superType.foreachIndex(action);
        for (Constraint constraint : getKlass().getIndices()) {
            if (constraint instanceof Index index) {
                action.accept(new IndexRef(this, index));
            }
        }
    }

    public IndexRef findSelfIndex(Predicate<IndexRef> filter) {
        for (Constraint constraint : getKlass().getIndices()) {
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    public boolean isBean() {
        return getKlass().isBeanClass();
    }

    @Override
    public boolean isReference() {
        return !getKlass().isValueKlass();
    }

    public boolean isAnonymous() {
        return getKlass().isAnonymous();
    }
}
