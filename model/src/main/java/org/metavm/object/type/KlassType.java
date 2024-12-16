package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.ClassTypeKey;
import org.metavm.object.type.rest.dto.ParameterizedTypeKey;
import org.metavm.object.type.rest.dto.TaggedClassTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public class KlassType extends ClassType {

    public static ClassType create(Klass klass, List<? extends Type> typeArguments) {
        assert klass.getDeclaringKlass() == null;
        return create(null, klass, typeArguments);
    }

    public static ClassType create(@Nullable GenericDeclarationRef owner, Klass klass, List<? extends Type> typeArguments) {
        if (typeArguments.equals(klass.getDefaultTypeArguments()))
            typeArguments = List.of();
        if(klass.getEnclosingFlow() != null && owner == null)
            throw new IllegalArgumentException("Missing owner for local class type " + klass);
        return new KlassType(owner, klass, typeArguments);
    }

    public static final Logger logger = LoggerFactory.getLogger(KlassType.class);

    private final @Nullable GenericDeclarationRef owner;
    private final Klass klass;
    private final @Nullable ValueArray<Type> typeArguments;
    @CopyIgnore
    private transient TypeSubstitutor substitutor;
    @CopyIgnore
    private transient TypeMetadata typeMetadata;

    public KlassType(@Nullable GenericDeclarationRef owner, @NotNull Klass klass, List<? extends Type> typeArguments) {
        assert klass.getEnclosingFlow() == null && klass.getDeclaringKlass() == null || owner != null :
            "owner is missing for local or inner class type " + klass.getName();
        this.owner = owner;
        this.klass = klass;
        this.typeArguments = typeArguments.isEmpty() ? null : new ValueArray<>(Type.class, typeArguments);
    }

    @Override
    public Type substitute(Type type) {
        if (typeArguments != null) {
            if (substitutor == null)
                substitutor = new TypeSubstitutor(NncUtils.map(getKlass().getTypeParameters(), TypeVariable::getType), typeArguments.toList());
            return type.accept(substitutor);
        } else
            return type;
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof KlassType that && Objects.equals(owner, that.owner) && getKlass() == that.getKlass() &&
                Objects.equals(typeArguments, that.typeArguments);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if (isParameterized())
            return getKlass().getQualifiedName() + "<" + NncUtils.join(typeArguments, type -> type.getInternalName(current)) + ">";
        else
            return getKlass().getQualifiedName();
    }

    @Override
    public boolean isCaptured() {
        return typeArguments != null && NncUtils.anyMatch(typeArguments, Type::isCaptured);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, getKlass(), typeArguments);
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
            if (owner != null)
                owner.write(output);
            else
                output.write(WireTypes.NULL);
            output.writeEntityId(klass);
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
        action.accept(klass);
        getTypeArguments().forEach(t -> t.forEachTypeDef(action));
    }

    public int getTypeTag() {
        return typeArguments == null && klass.getTag() < 1000000 ? (int) getKlass().getTag() : 0;
    }

    public TypeMetadata getTypeMetadata() {
        if (typeMetadata == null) {
//            logger.debug("Getting type metadata for type {}, owner: {}", this, owner);
            typeMetadata = klass.getTypeMetadata(getAllTypeArguments());
        }
        return typeMetadata;
    }

    @Override
    public String getTypeDesc() {
        String prefix;
        if (owner != null)
            prefix = owner.getTypeDesc() + "." + klass.getName();
        else
            prefix = klass.getTypeDesc();
        return prefix + (typeArguments != null ? "<" + NncUtils.join(typeArguments, Type::getTypeDesc) + ">" : "");
    }

    @Override
    public Klass getKlass() {
        return klass;
    }

    @Override
    public List<Type> getTypeArguments() {
        // the type arguments should be the list of type parameters for a raw ClassType
        return typeArguments != null ? typeArguments.toList() : klass.getDefaultTypeArguments();
    }

    public static ClassType read(MvInput input) {
        return new KlassType(null, input.getKlass(input.readId()), List.of());
    }

    public static ClassType readTagged(MvInput input) {
        var type = new KlassType(null, input.getKlass(input.readId()), List.of());
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
        return new KlassType(owner, klass, typeArgs);
    }

    @Override
    @Nullable
    public GenericDeclarationRef getOwner() {
        return owner;
    }

}
