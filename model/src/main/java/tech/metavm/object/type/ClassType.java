package tech.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.entity.ValueArray;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@EntityType
public class ClassType extends Type implements ISubstitutor {

    public static final Logger logger = LoggerFactory.getLogger(ClassType.class);

    private final Klass klass;
    private final ValueArray<Type> typeArguments;
    private transient TypeSubstitutor substitutor;
    private transient Klass resolved;

    public ClassType(Klass klass, List<Type> typeArguments) {
//        if (klass.isParameterized())
//            throw new InternalException("Can not use a parameterized klass for a ClassType. klass: " + klass.getTypeDesc());
//        if(typeArguments.equals(NncUtils.map(klass.getTypeParameters(), TypeVariable::getType)))
//            throw new InternalException("Trying to create an raw class type using type arguments for klass: " + klass.getTypeDesc());
        this.klass = klass;
        this.typeArguments = new ValueArray<>(Type.class, typeArguments);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
        return typeArguments.isEmpty() ?
                (klass.getTag() > 0 ?
                        new TaggedClassTypeKey(getTypeDefId.apply(klass), klass.getTag()) :
                        new ClassTypeKey(getTypeDefId.apply(klass))
                ) :
                new ParameterizedTypeKey(klass.getId(), NncUtils.map(typeArguments, type -> type.toTypeKey(getTypeDefId)));
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

    public Type getListElementType() {
        return resolve().getListElementType();
    }

    public List<Type> getTypeArguments() {
        // the type arguments should be the list of type parameters for a raw ClassType
        return isParameterized() ? typeArguments.toList() : klass.getTypeArguments();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof ClassType thatClassType) {
            if (typeArguments.isEmpty() && thatClassType.typeArguments.isEmpty() && klass == thatClassType.klass)
                return true;
            if (!typeArguments.isEmpty()) {
                var thatAncestor = thatClassType.findAncestor(klass);
                if (thatAncestor != null)
                    return NncUtils.biAllMatch(typeArguments, thatAncestor.typeArguments, Type::contains);
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
        return resolve().findAncestor(template).getType();
    }

    @Override
    public Type substitute(Type type) {
        if (!typeArguments.isEmpty()) {
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
        if (typeArguments.isEmpty()) {
            resolved = klass;
            return klass;
        } else {
            return resolved = klass.getParameterized(typeArguments.toList());
        }
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof ClassType that && klass == that.klass && typeArguments.equals(that.typeArguments);
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
    public boolean isViewType(Type type) {
        return resolve().isViewType(type);
    }

    @Override
    public String getName() {
        return Types.getParameterizedName(klass.getName(), typeArguments.toList());
    }

    @Nullable
    @Override
    public String getCode() {
        return Types.getParameterizedCode(klass.getCode(), typeArguments.toList());
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
        return resolve().getInternalName(current);
    }

    @Override
    public boolean isCaptured() {
        return NncUtils.anyMatch(typeArguments, Type::isCaptured);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        var id = getTypeDefExpr == null ? Constants.ID_PREFIX + serializeContext.getStringId(klass) : getTypeDefExpr.apply(klass);
        var tag = klass.getTag();
        return typeArguments.isEmpty() ? (tag == 0 ? id : id + ":" + tag)
                : id + "<" + NncUtils.join(typeArguments, type -> type.toExpression(serializeContext, getTypeDefExpr)) + ">";
    }

    @Override
    public int getTypeKeyCode() {
        return typeArguments.isEmpty() ? (klass.getTag() == 0 ? TypeKeyCodes.CLASS : TypeKeyCodes.TAGGED_CLASS) : TypeKeyCodes.PARAMETERIZED;
    }

    @Override
    public void write(InstanceOutput output) {
        if (typeArguments.isEmpty()) {
            var tag = klass.getTag();
            if (tag == 0) {
                output.write(TypeKeyCodes.CLASS);
                output.writeId(klass.getId());
            } else {
                output.write(TypeKeyCodes.TAGGED_CLASS);
                output.writeId(klass.getId());
                output.writeInt(tag);
            }
        } else {
            output.write(TypeKeyCodes.PARAMETERIZED);
            output.writeId(klass.getId());
            output.writeInt(typeArguments.size());
            typeArguments.forEach(t -> t.write(output));
        }
    }

    public static ClassType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        return new ClassType(typeDefProvider.getKlass(input.readId()), List.of());
    }

    public static ClassType readTagged(InstanceInput input, TypeDefProvider typeDefProvider) {
        var type = new ClassType(typeDefProvider.getKlass(input.readId()), List.of());
        input.readInt();
        return type;
    }

    public static ClassType readParameterized(InstanceInput input, TypeDefProvider typeDefProvider) {
        var klass = typeDefProvider.getKlass(input.readId());
        int numTypeArgs = input.readInt();
        var typeArgs = new ArrayList<Type>(numTypeArgs);
        for (int i = 0; i < numTypeArgs; i++)
            typeArgs.add(Type.readType(input, typeDefProvider));
        return new ClassType(klass, typeArgs);
    }

    public boolean isParameterized() {
        return !typeArguments.isEmpty();
    }

    public boolean isList() {
        return klass.isList();
    }

    public boolean isChildList() {
        return klass.isChildList();
    }

    @Override
    public <S> void acceptComponents(TypeVisitor<?, S> visitor, S s) {
        typeArguments.forEach(t -> t.accept(visitor, s));
    }

    @Override
    public void forEachTypeDef(Consumer<TypeDef> action) {
        action.accept(klass);
        typeArguments.forEach(t -> t.forEachTypeDef(action));
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
        return klass.getTag();
    }

    @Override
    public boolean isValue() {
        return klass.getKind() == ClassKind.VALUE;
    }
}
