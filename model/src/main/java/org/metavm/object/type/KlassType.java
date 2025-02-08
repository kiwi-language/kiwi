package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.NullValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.ClassTypeKey;
import org.metavm.object.type.rest.dto.ParameterizedTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
@Slf4j
public class KlassType extends ClassType {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static ClassType create(Klass klass, List<? extends Type> typeArguments) {
        assert klass.getScope() == null;
        return create(null, klass, typeArguments);
    }

    public static ClassType create(@Nullable GenericDeclarationRef owner, Klass klass, List<? extends Type> typeArguments) {
        if (typeArguments.equals(klass.getDefaultTypeArguments()))
            typeArguments = List.of();
        if(klass.isLocal() && owner == null)
            throw new IllegalArgumentException("Missing owner for local class type " + klass);
        return new KlassType(owner, klass, typeArguments);
    }

    public final @Nullable GenericDeclarationRef owner;
    private final Reference klassReference;
    private final @Nullable List<Type> typeArguments;
    @CopyIgnore
    private transient TypeSubstitutor substitutor;
    @CopyIgnore
    private transient TypeMetadata typeMetadata;

    public KlassType(@Nullable GenericDeclarationRef owner, @NotNull Klass klass, List<? extends Type> typeArguments) {
//        assert klass.getEnclosingFlow() == null && klass.getDeclaringKlass() == null || owner != null :
//            "owner is missing for local or inner class type " + klass.getName();
        this(owner, klass, typeArguments, typeArguments.isEmpty() && klass.getTag() < 1000000 ? (int) klass.getTag() : 0);
    }

    public KlassType(@Nullable GenericDeclarationRef owner, @NotNull Klass klass, List<? extends Type> typeArguments, int typeTag) {
        this(owner, klass.getReference(), typeArguments, typeTag);
    }

    public KlassType(@Nullable GenericDeclarationRef owner, @NotNull Reference klassReference, List<? extends Type> typeArguments, int typeTag) {
        this.owner = owner;
        this.klassReference = klassReference;
        this.typeArguments = typeArguments.isEmpty() ? null : new ArrayList<>(typeArguments);
    }


    @Override
    public Type substitute(Type type) {
        if (typeArguments != null) {
            if (substitutor == null)
                substitutor = new TypeSubstitutor(Utils.map(getKlass().getTypeParameters(), TypeVariable::getType), typeArguments);
            return type.accept(substitutor);
        } else
            return type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KlassType that && Objects.equals(owner, that.owner) && klassReference.equals(that.klassReference) &&
                Objects.equals(typeArguments, that.typeArguments);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if (isParameterized())
            return getKlass().getQualifiedName() + "<" + Utils.join(typeArguments, type -> type.getInternalName(current)) + ">";
        else
            return getKlass().getQualifiedName();
    }

    @Override
    public boolean isCaptured() {
        return owner instanceof KlassType ownerKt && ownerKt.isCaptured() ||
                typeArguments != null && Utils.anyMatch(typeArguments, Type::isCaptured);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, klassReference, typeArguments);
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return owner == null && typeArguments == null ?
                new ClassTypeKey(getTypeDefId.apply(getKlass()))
                :
                new ParameterizedTypeKey(
                        owner != null ? owner.toGenericDeclarationKey(getTypeDefId) : null,
                        klassReference.getId(), Utils.map(typeArguments, type -> type.toTypeKey(getTypeDefId)));
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var id = getTypeDefExpr == null ? Constants.ID_PREFIX + serializeContext.getStringId(getKlass()) : getTypeDefExpr.apply(getKlass());
        return typeArguments == null ?  id
                : id + "<" + Utils.join(typeArguments, type -> type.toExpression(serializeContext, getTypeDefExpr)) + ">";
    }

    @Override
    public int getTypeKeyCode() {
        return typeArguments == null ? WireTypes.CLASS_TYPE : WireTypes.PARAMETERIZED_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        if (owner == null && typeArguments == null) {
            output.write(WireTypes.CLASS_TYPE);
            output.writeReference(klassReference);
        } else {
            output.write(WireTypes.PARAMETERIZED_TYPE);
            if (owner != null)
                owner.write(output);
            else
                output.write(WireTypes.NULL);
            output.writeReference(klassReference);
            if (typeArguments == null)
                output.writeInt(0);
            else {
                output.writeInt(typeArguments.size());
                typeArguments.forEach(t -> t.write(output));
            }
        }
    }

    public boolean isParameterized() {
        return typeArguments != null;
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        if (owner != null)
            owner.forEachTypeDef(action);
        action.accept(getKlass());
        getTypeArguments().forEach(t -> t.forEachTypeDef(action));
    }

    public TypeMetadata getTypeMetadata() {
        if (typeMetadata == null) {
//            logger.debug("Getting type metadata for type {}, owner: {}", this, owner);
            typeMetadata = getKlass().getTypeMetadata(getAllTypeArguments());
        }
        return typeMetadata;
    }

    @Override
    public String getTypeDesc() {
        String prefix;
        if (owner != null)
            prefix = owner.getTypeDesc() + "." + getKlass().getName();
        else
            prefix = getKlass().getTypeDesc();
        return prefix + (typeArguments != null ? "<" + Utils.join(typeArguments, Type::getTypeDesc) + ">" : "");
    }

    @Override
    public Klass getKlass() {
    return (Klass) klassReference.get();
    }

    public Id getKlassId() {
        return klassReference.getId();
    }

    @Override
    public List<Type> getTypeArguments() {
        // the type arguments should be the list of type parameters for a raw ClassType
        return typeArguments != null ? Collections.unmodifiableList(typeArguments) : getKlass().getDefaultTypeArguments();
    }

    public static ClassType read(MvInput input) {
        return new KlassType(null, input.readReference(), List.of(), 0);
    }

    public static ClassType readParameterized(MvInput input) {
        var ownerValue = input.readValue();
        var owner = ownerValue instanceof NullValue ? null : (GenericDeclarationRef) ownerValue;
        var klass = input.readReference();
        int numTypeArgs = input.readInt();
        var typeArgs = new ArrayList<Type>(numTypeArgs);
        for (int i = 0; i < numTypeArgs; i++)
            typeArgs.add(input.readType());
        return new KlassType(owner, klass, typeArgs, 0);
    }

    @Override
    @Nullable
    public GenericDeclarationRef getOwner() {
        return owner;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitKlassType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitKlassType(this, s);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        if (owner != null) owner.accept(visitor);
        if (typeArguments != null) typeArguments.forEach(arg -> arg.accept(visitor));
    }

    @Override
    public ColumnKind getSQLType() {
        return ColumnKind.REFERENCE;
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (owner != null) owner.forEachReference(action);
        action.accept(klassReference);
        if (typeArguments != null) for (var typeArguments_ : typeArguments) typeArguments_.forEachReference(action);
    }
}
