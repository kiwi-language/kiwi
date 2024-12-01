package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.MethodRefKey;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType
@Slf4j
public class MethodRef extends FlowRef implements PropertyRef {

    public static MethodRef create(ClassType declaringType, Method method, List<? extends Type> typeArguments) {
        if(typeArguments.equals(method.getDefaultTypeArguments()))
            typeArguments = List.of();
        return new MethodRef(declaringType, method, typeArguments);
    }

    private final ClassType declaringType;

    public MethodRef(ClassType declaringType, @NotNull Method rawFlow, List<? extends Type> typeArguments) {
        super(rawFlow, typeArguments);
        assert declaringType.getKlass() != DummyKlass.INSTANCE;
        this.declaringType = declaringType;
    }

    public List<Type> getAllTypeArguments() {
        var typeArgs = new ArrayList<>(declaringType.getAllTypeArguments());
        typeArgs.addAll(getTypeArguments());
        return typeArgs;
    }

    @Override
    public GenericDeclarationRef getOwner() {
        return getDeclaringType();
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    @Override
    public Method getRawFlow() {
        return (Method) super.getRawFlow();
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MethodRef methodRef)) return false;
        if (!super.equals0(obj)) return false;
        return Objects.equals(declaringType, methodRef.declaringType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), declaringType);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethodRef(this);
    }

    public MethodRefKey toDTO(SerializeContext serializeContext, Function<ITypeDef, String> getTypeDefId) {
        return new MethodRefKey(
                declaringType.toExpression(serializeContext, getTypeDefId),
                serializeContext.getStringId(getRawFlow()),
                NncUtils.map(typeArguments, t -> t.toExpression(serializeContext, getTypeDefId))
        );
    }

    @Override
    protected String toString0() {
        return getTypeDesc();
    }

    public static MethodRef read(MvInput input) {
       var classType = (ClassType) Type.readType(input);
       var rawMethod = input.getMethod(input.readId());
       var typeArgsCount = input.readInt();
       var typeArgs = new ArrayList<Type>(typeArgsCount);
       for (int i = 0; i < typeArgsCount; i++) {
           typeArgs.add(Type.readType(input));
       }
       return new MethodRef(classType, rawMethod, typeArgs);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.METHOD_REF);
        declaringType.write(output);
        output.writeEntityId(getRawFlow());
        output.writeInt(typeArguments.size());
        for (Type typeArgument : typeArguments) {
            typeArgument.write(output);
        }
    }

    @Override
    public GenericDeclarationRefKey toGenericDeclarationKey(Function<ITypeDef, Id> getTypeDefId) {
        try(var serContext = SerializeContext.enter()) {
            return toDTO(serContext, typeDef -> Constants.addIdPrefix(getTypeDefId.apply(typeDef).toString()));
        }
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return declaringType.toExpression(serializeContext, getTypeDefExpr) + "::"
                + (
                        getTypeDefExpr != null ? getTypeDefExpr.apply(getRawFlow()) :
                                Constants.addIdPrefix(serializeContext.getStringId(getRawFlow()))
                )
                + (
                        typeArguments.isEmpty() ? "" :
                                "<" + NncUtils.join(
                                        typeArguments,
                                        t -> t.toExpression(serializeContext, getTypeDefExpr),
                                        ","
                                ) + ">"
                );
    }

    public String getName() {
        return getRawFlow().getName();
    }

    public String getTypeDesc() {
        return declaringType + "."
                + getRawFlow().getName()
                + (typeArguments.isEmpty() ? "" : "<" + NncUtils.join(typeArguments, Type::getTypeDesc) + ">")
                + "(" + NncUtils.join(getRawFlow().getParameterTypes(), Type::getTypeDesc) + ")";
    }

    public boolean isHiddenBy(MethodRef that) {
        var paramTypes = getParameterTypes();
        var thatParamTypes = that.getParameterTypes();
        if (paramTypes.size() != thatParamTypes.size())
            return false;
        if (paramTypes.equals(thatParamTypes)) {
            if (declaringType.equals(that.getDeclaringType()))
                throw new InternalException(
                        String.format("Methods with the same signature defined in the same type: %s(%s)",
                                getName(), NncUtils.join(paramTypes, Type::getTypeDesc)));
            return declaringType.isAssignableFrom(that.getDeclaringType());
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            var paramType = paramTypes.get(i);
            var thatParamType = thatParamTypes.get(i);
            if (!paramType.isConvertibleFrom(thatParamType))
                return false;
        }
        return true;
    }

    public boolean isOverrideOf(MethodRef methodRef) {
        var m1 = getRawFlow();
        var m2 = methodRef.getRawFlow();
        if(m2.getName().equals("test") && m2.getDeclaringType().getName().equals("Predicate")
                && toString().equals("null.test(capturedtypes.CtFoo|null)")) {
            System.out.println("Caught");
        }
        if(m1.isConstructor() || m2.isConstructor()
                || m1.isStatic() || m2.isStatic()
                || m1.isPrivate() || m2.isPrivate())
            return false;
        if(m1.getName().equals(m2.getName())
                && m1.getParameters().size() == m2.getParameters().size()
                && m1.getTypeParameters().size() == m2.getTypeParameters().size()
        ) {
            var k1 = getDeclaringType();
            var k2 = methodRef.getDeclaringType();
            if(!k1.equals(k2) && (k2.isInterface() || k2.isAssignableFrom(k1))) {
                var subst = new TypeSubstitutor(
                        NncUtils.map(m1.getTypeParameters(), TypeVariable::getType),
                        NncUtils.map(m2.getTypeParameters(), TypeVariable::getType)
                );
                if (NncUtils.biAllMatch(getParameterTypes(), methodRef.getParameterTypes(),
                        (t1, t2) -> t1.accept(subst).equals(t2))) {
//                    NncUtils.requireTrue(method.getReturnType().isAssignableFrom(getReturnType()),
//                            () -> "Return type of the overriding method " + getQualifiedSignature()
//                                    + " (" + getReturnType() + ") is not assignable "
//                                    + " to the return type of the overridden method " + method.getQualifiedSignature()
//                                    + " (" + method.getReturnType() + ")");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isVirtual() {
        return getRawFlow().isVirtual();
    }

    public boolean isAbstract() {
        return getRawFlow().isAbstract();
    }

    public boolean isInstanceMethod() {
        return getRawFlow().isInstanceMethod();
    }

    public @Nullable FunctionType getStaticType() {
        try {
            var idx = getRawFlow().getStaticTypeIndex();
            return idx == -1 ? null : (FunctionType) getTypeMetadata().getType(idx);
        }
        catch (ClassCastException e) {
            throw new IllegalStateException("Failed to static type for method " + getRawFlow() + ". Static type index: " + getRawFlow().getStaticTypeIndex()
                    + ", constants: " + getTypeMetadata() + ", template constants: " + getRawFlow().getConstantPool());
        }
    }

    public Type getParameterType(int index) {
        return getTypeMetadata().getType(getRawFlow().getParameter(index).getTypeIndex());
    }

    public boolean isStatic() {
        return getRawFlow().isStatic();
    }

    public boolean isConstructor() {
        return getRawFlow().isConstructor();
    }

    public MethodRef getParameterized(List<? extends Type> typeArguments) {
        return create(declaringType, getRawFlow(), typeArguments);
    }

    public List<TypeVariable> getTypeParameters() {
        return getRawFlow().getTypeParameters();
    }

    public boolean isPublic() {
        return getRawFlow().isPublic();
    }

    public String getQualifiedSignature() {
        return getRawFlow().getQualifiedSignature();
    }

    public List<ParameterRef> getParameters() {
        return NncUtils.map(getRawFlow().getParameters(), p -> new ParameterRef(this, p));
    }

    @Override
    public Property getProperty() {
         return getRawFlow();
    }

    public TypeMetadata getTypeMetadata0() {
//        if(toString0().equals("Collection<E>.toArray")) {
//            var typeArg = (VariableType) declaringType.getTypeArguments().get(0);
//            log.debug("Generating type metadata for {}, constant pool size: {}", this, getRawFlow().getConstantPool().size());
//            log.debug("Generic declaration: {}, method: {}, typee argumetns: {}",
//                    typeArg.getVariable().getGenericDeclaration(), getRawFlow().getQualifiedSignature(),
//                    typeArguments.toList());
//        }
//        if(toString().equals("java.util.Collection<java.util.AbstractCollection.E>.toArray()") && DebugEnv.methodRef != null) {
//            System.out.println("Caught");
//        }
        var m = getRawFlow().getTypeMetadata(getAllTypeArguments());
//        log.debug("Generating type metadata for " + this);
//        if(toString().equals("java.util.Collection<java.util.AbstractCollection.E>.toArray()")) {
//            if(DebugEnv.methodRef != null)
//                log.debug("Equals to last method ref: {}", DebugEnv.methodRef.equals(this));
//            else
//                DebugEnv.methodRef = this;
//            log.debug("Generated type metadata {} for {}@{}, size: {}",
//                    System.identityHashCode(m),
//                    this, System.identityHashCode(this),
//                    m.size());
//        }
        return m;
    }

}
