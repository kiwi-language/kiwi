package tech.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.type.rest.dto.ClassTypeKey;
import tech.metavm.object.type.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
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

@EntityType("ClassType")
public class ClassType extends Type implements ISubstitutor {

    public static final Logger logger = LoggerFactory.getLogger(ClassType.class);

    private final Klass klass;
    @ChildEntity("typeArguments")
    private final ChildArray<Type> typeArguments = addChild(new ChildArray<>(Type.class), "typeArguments");
    private transient TypeSubstitutor substitutor;
    private transient Klass resolved;

    public ClassType(Klass klass, List<Type> typeArguments) {
//        if (klass.isParameterized())
//            throw new InternalException("Can not use a parameterized klass for a ClassType. klass: " + klass.getTypeDesc());
//        if(typeArguments.equals(NncUtils.map(klass.getTypeParameters(), TypeVariable::getType)))
//            throw new InternalException("Trying to create an raw class type using type arguments for klass: " + klass.getTypeDesc());
        this.klass = klass;
        this.typeArguments.addChildren(NncUtils.map(typeArguments, Type::copy));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, String> getTypeDefId) {
        return typeArguments.isEmpty() ?
                new ClassTypeKey(getTypeDefId.apply(klass)) :
                new ParameterizedTypeKey(klass.getStringId(), NncUtils.map(typeArguments, type -> type.toTypeKey(getTypeDefId)));
    }

    public Klass getKlass() {
        return klass;
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
        }
        else {
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

    public ClassType copy() {
        var copy = new ClassType(klass, NncUtils.map(typeArguments, Type::copy));
        copy.resolved = resolved;
        return copy;
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        var id = getTypeDefExpr == null ? Constants.CONSTANT_ID_PREFIX + serializeContext.getId(klass) : getTypeDefExpr.apply(klass);
        return typeArguments.isEmpty() ? id : id + "<" + NncUtils.join(typeArguments, type -> type.toExpression(serializeContext, getTypeDefExpr)) + ">";
    }

    @Override
    public int getTypeKeyCode() {
        return typeArguments.isEmpty() ? TypeKeyCodes.CLASS : TypeKeyCodes.PARAMETERIZED;
    }

    @Override
    public void write0(InstanceOutput output) {
        output.writeId(klass.getId());
        if (!typeArguments.isEmpty()) {
            output.writeInt(typeArguments.size());
            typeArguments.forEach(t -> t.write(output));
        }
    }

    public static ClassType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var klass = typeDefProvider.getKlass(input.readId());
        var typeArgs = new ArrayList<Type>();
        for (int i = 0; i < input.readInt(); i++)
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
}
