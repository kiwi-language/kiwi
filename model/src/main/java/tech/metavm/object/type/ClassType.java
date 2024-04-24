package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.type.rest.dto.ClassTypeKey;
import tech.metavm.object.type.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EntityType("ClassType")
public class ClassType extends Type implements ISubstitutor {
    private final Klass klass;
    private final ChildArray<Type> typeArguments = addChild(new ChildArray<>(Type.class), "typeArguments");
    private transient TypeSubstitutor substitutor;
    private transient Klass resolved;

    public ClassType(Klass klass, List<Type> typeArguments) {
        super(Types.getParameterizedName(klass.getName(), typeArguments),
                Types.getParameterizedCode(klass.getCode(), typeArguments), false, false, klass.getKind().typeCategory());
        this.klass = klass;
        this.typeArguments.addChildren(NncUtils.map(typeArguments, Type::copy));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    @Override
    public TypeKey getTypeKey() {
        return typeArguments.isEmpty() ?
                new ClassTypeKey(klass.getStringId()) :
                new ParameterizedTypeKey(klass.getStringId(), NncUtils.map(typeArguments, Type::getTypeKey));
    }

    public Klass getKlass() {
        return klass;
    }

    public ClassType getEffectiveTemplate() {
        return klass.getEffectiveTemplate().getType();
    }

    public Type getListElementType() {
        return klass.getListElementType();
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    @Override
    protected boolean isAssignableFrom0(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        if (that instanceof ClassType thatClassType) {
            if (typeArguments.isEmpty() && thatClassType.typeArguments.isEmpty() && klass == thatClassType.klass)
                return true;
            if(!typeArguments.isEmpty()) {
                var thatAncestor = thatClassType.findAncestor(klass);
                if(thatAncestor != null)
                    return isAssignableFrom(thatAncestor, typeMapping);
                else
                    return false;
            }
            else {
                var thatSuper = thatClassType.getSuperType();
                if(thatSuper != null && isAssignableFrom(thatSuper, typeMapping))
                    return true;
                if(isInterface()) {
                    for (ClassType thatInterface : thatClassType.getInterfaces()) {
                        if (isAssignableFrom(thatInterface, typeMapping))
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
        var superClass = klass.getSuperClass();
        if(superClass != null)
            return (ClassType) substitute(superClass.getType());
        else
            return null;
    }

    public List<ClassType> getInterfaces() {
        return NncUtils.map(klass.getInterfaces(), it -> (ClassType) substitute(it.getType()));
    }

    public @Nullable ClassType findAncestor(Klass template) {
        return NncUtils.get(klass.findAncestor(template), a -> (ClassType) substitute(a.getType()));
    }

    @Override
    public Type substitute(Type type) {
        if(substitutor == null)
            substitutor = new TypeSubstitutor(NncUtils.map(klass.getTypeParameters(), TypeVariable::getType), typeArguments.toList());
        return type.accept(substitutor);
    }

    public Klass resolve() {
        if(resolved != null)
            return resolved;
        if(typeArguments.isEmpty())
            return resolved = klass;
        else
            return resolved = klass.getParameterized(typeArguments.toList());
    }

    @Override
    public boolean equals(Type that, @Nullable Map<TypeVariable, ? extends Type> mapping) {
        if(that instanceof ClassType thatClassType)
            return klass == thatClassType.klass && typeArguments.equals(thatClassType.typeArguments);
        else
            return false;
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
        if(!typeArguments.isEmpty()) {
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
