package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
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

@EntityType
public class ClassType extends CompositeType implements ISubstitutor, GenericDeclarationRef {

    public static final Logger logger = LoggerFactory.getLogger(ClassType.class);

    private final Klass klass;
    private final @Nullable ValueArray<Type> typeArguments;
    @CopyIgnore
    private transient TypeSubstitutor substitutor;
    @CopyIgnore
    private transient Klass resolved;

    public ClassType(@NotNull Klass klass, List<Type> typeArguments) {
//        if (klass.isParameterized())
//            throw new InternalException("Can not use a parameterized klass for a ClassType. klass: " + klass.getTypeDesc());
//        if(typeArguments.equals(NncUtils.map(klass.getTypeParameters(), TypeVariable::getType)))
//            throw new InternalException("Trying to create an raw class type using type arguments for klass: " + klass.getTypeDesc());
//        if(klass == DummyKlass.INSTANCE)
//            throw new IllegalArgumentException("Creating ClassType with dummy klass");
        this.klass = klass;
        this.typeArguments = typeArguments.isEmpty() ? null : new ValueArray<>(Type.class, typeArguments);
    }

    public ClassType trySimplify() {
        if(!EntityProxyFactory.isDummy(klass)
                && typeArguments != null && klass.getTypeArguments().equals(typeArguments.toList()))
            return klass.getType();
        else
            return this;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return typeArguments == null ?
                (getTypeTag() > 0 ?
                        new TaggedClassTypeKey(getTypeDefId.apply(klass), getTypeTag()) :
                        new ClassTypeKey(getTypeDefId.apply(klass))
                ) :
                new ParameterizedTypeKey(klass.getId(), NncUtils.map(typeArguments, type -> type.toTypeKey(getTypeDefId)));
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

    public ClassType getEffectiveTemplate() {
        return klass.getType();
    }

    public Type getFirstTypeArgument() {
        return resolve().getFirstTypeArgument();
    }

    public Type getIterableElementType() {
        return resolve().getIterableElementType();
    }

    public @Nullable ClassType findAncestorType(Klass rawKlass) {
        return NncUtils.get(resolve().findAncestorByTemplate(rawKlass), Klass::getType);
    }

    public List<Type> getTypeArguments() {
        // the type arguments should be the list of type parameters for a raw ClassType
        return typeArguments != null ? typeArguments.toList() : klass.getTypeArguments();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof ClassType thatClassType) {
            if (typeArguments == null && thatClassType.typeArguments == null && klass == thatClassType.klass)
                return true;
            if (typeArguments != null) {
                var thatAncestor = thatClassType.findAncestor(klass);
                if (thatAncestor != null)
                    return NncUtils.biAllMatch(typeArguments, thatAncestor.getTypeArguments(), Type::contains);
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
                    return false;
                }
            }
        }
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
        return resolve().getSuperType();
    }

    public List<ClassType> getInterfaces() {
        return resolve().getInterfaces();
    }

    public @Nullable ClassType findAncestor(Klass template) {
        var ancestor =  resolve().findAncestorByTemplate(template);
        return ancestor != null ? ancestor.getType() : null;
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

    public Klass resolve() {
        if (resolved != null) {
            return resolved;
        }
        if (typeArguments == null) {
            resolved = klass;
            return klass;
        } else {
            return resolved = klass.getParameterized(typeArguments.toList());
        }
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof ClassType that && klass == that.klass &&
                Objects.equals(typeArguments, that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(klass, typeArguments);
    }

    @Override
    public boolean isEnum() {
        return klass.isEnum();
    }

    @Override
    public String getName() {
        if (typeArguments == null)
            return klass.getQualifiedName();
        else
            return Types.getParameterizedName(klass.getName(), typeArguments.toList());
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
        if (typeArguments == null) {
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
            output.writeEntityId(klass);
            output.writeInt(typeArguments.size());
            typeArguments.forEach(t -> t.write(output));
        }
    }

    public static ClassType read(MvInput input) {
        return new ClassType(input.getKlass(input.readId()), List.of());
    }

    public static ClassType readTagged(MvInput input) {
        var type = new ClassType(input.getKlass(input.readId()), List.of());
        input.readLong();
        return type;
    }

    public static ClassType readParameterized(MvInput input) {
        var klass = input.getKlass(input.readId());
        int numTypeArgs = input.readInt();
        var typeArgs = new ArrayList<Type>(numTypeArgs);
        for (int i = 0; i < numTypeArgs; i++)
            typeArgs.add(Type.readType(input));
        return new ClassType(klass, typeArgs);
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
        action.accept(klass);
        getTypeArguments().forEach(t -> t.forEachTypeDef(action));
    }

    @Override
    public String getTypeDesc() {
        return resolve().getTypeDesc();
    }

    @Override
    public TypeId getTypeId() {
        return new TypeId(TypeTag.fromCategory(getCategory()), resolve().getId().getTreeId());
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
}
