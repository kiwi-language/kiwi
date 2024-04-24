package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.type.rest.dto.ClassTypeKey;
import tech.metavm.object.type.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType("ClassType")
public class ClassType extends Type implements ISubstitutor {

    public static final Logger logger = LoggerFactory.getLogger(ClassType.class);

    private final Klass klass;
    private final ChildArray<Type> typeArguments = addChild(new ChildArray<>(Type.class), "typeArguments");
    private transient TypeSubstitutor substitutor;
    private transient Klass resolved;

    public ClassType(Klass klass, List<Type> typeArguments) {
        super(Types.getParameterizedName(klass.getName(), typeArguments),
                Types.getParameterizedCode(klass.getCode(), typeArguments), false, false, klass.getKind().typeCategory());
        if (klass.isParameterized())
            throw new InternalException("Can not use a parameterized klass for a ClassType. klass: " + klass.getTypeDesc());
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
    public TypeKey toTypeKey() {
        return typeArguments.toList().equals(NncUtils.map(klass.getTypeParameters(), TypeVariable::getType)) ?
                new ClassTypeKey(klass.getStringId()) :
                new ParameterizedTypeKey(klass.getStringId(), NncUtils.map(typeArguments, Type::toTypeKey));
    }

    public Klass getKlass() {
        return klass;
    }

    public ClassType getEffectiveTemplate() {
        return klass.getType();
    }

    public Type getListElementType() {
        return klass.getListElementType();
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
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

    public boolean isInterface() {
        return klass.isInterface();
    }

    public @Nullable ClassType getSuperType() {
        var rawSuperType = klass.getSuperType();
        return rawSuperType != null ? (ClassType) substitute(rawSuperType) : null;
    }

    public List<ClassType> getInterfaces() {
        return NncUtils.map(klass.getInterfaces(), it -> (ClassType) substitute(it));
    }

    public @Nullable ClassType findAncestor(Klass template) {
        return NncUtils.get(klass.findAncestor(template), a -> (ClassType) substitute(a.getType()));
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
        if (resolved != null)
            return resolved;
        if (typeArguments.isEmpty())
            return resolved = klass;
        else
            return resolved = klass.getParameterized(typeArguments.toList());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClassType that && klass == that.klass && typeArguments.equals(that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(klass, typeArguments);
    }

    @Override
    protected TypeParam getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return null;
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return klass.getInternalName(current);
    }

    @Override
    public boolean isCaptured() {
        return NncUtils.anyMatch(typeArguments, Type::isCaptured);
    }

    public ClassType copy() {
        return new ClassType(klass, NncUtils.map(typeArguments, Type::copy));
    }

    @Override
    public String toTypeExpression(SerializeContext serializeContext) {
        var id = Constants.CONSTANT_ID_PREFIX + serializeContext.getId(klass);
        return typeArguments.isEmpty() ? id : id + "<" + NncUtils.map(typeArguments, type -> type.toTypeExpression(serializeContext)) + ">";
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

}
