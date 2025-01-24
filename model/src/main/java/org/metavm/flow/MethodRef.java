package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.MethodRefKey;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
@Slf4j
public class MethodRef extends FlowRef implements PropertyRef {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static MethodRef create(ClassType declaringType, Method method, List<? extends Type> typeArguments) {
        if(typeArguments.equals(method.getDefaultTypeArguments()))
            typeArguments = List.of();
        return new MethodRef(declaringType, method, typeArguments);
    }

    private final ClassType declaringType;

    public MethodRef(ClassType declaringType, @NotNull Method rawFlow, List<? extends Type> typeArguments) {
        super(rawFlow, typeArguments);
        this.declaringType = declaringType;
    }

    public MethodRef(ClassType declaringType, @NotNull Reference methodReference, List<? extends Type> typeArguments) {
        super(methodReference, typeArguments);
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MethodRef methodRef)) return false;
        if (!super.equals(obj)) return false;
        return Objects.equals(declaringType, methodRef.declaringType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), declaringType);
    }

    public MethodRefKey toDTO(SerializeContext serializeContext, Function<ITypeDef, String> getTypeDefId) {
        return new MethodRefKey(
                declaringType.toExpression(serializeContext, getTypeDefId),
                serializeContext.getStringId(getRawFlow()),
                Utils.map(typeArguments, t -> t.toExpression(serializeContext, getTypeDefId))
        );
    }

    @Override
    public String toString() {
        return getTypeDesc();
    }

    public static MethodRef read(MvInput input) {
       var classType = (ClassType) input.readType();
       var flowReference = input.readReference();
       var typeArgsCount = input.readInt();
       var typeArgs = new ArrayList<Type>(typeArgsCount);
       for (int i = 0; i < typeArgsCount; i++) {
           typeArgs.add(input.readType());
       }
       return new MethodRef(classType, flowReference, typeArgs);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.METHOD_REF);
        declaringType.write(output);
        output.writeReference(flowReference);
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
                                "<" + Utils.join(
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
                + (typeArguments.isEmpty() ? "" : "<" + Utils.join(typeArguments, Type::getTypeDesc) + ">")
                + "(" + Utils.join(getRawFlow().getParameterTypes(), Type::getTypeDesc) + ")";
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
                                getName(), Utils.join(paramTypes, Type::getTypeDesc)));
            return declaringType.isAssignableFrom(that.getDeclaringType());
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            var paramType = paramTypes.get(i);
            var thatParamType = thatParamTypes.get(i);
            if (!paramType.isAssignableFrom(thatParamType))
                return false;
        }
        return true;
    }

    public boolean isOverrideOf(MethodRef methodRef) {
        var m1 = getRawFlow();
        var m2 = methodRef.getRawFlow();
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
                        Utils.map(m1.getTypeParameters(), TypeVariable::getType),
                        Utils.map(m2.getTypeParameters(), TypeVariable::getType)
                );
                if (Utils.biAllMatch(getParameterTypes(), methodRef.getParameterTypes(),
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
        return getRawFlow().getStaticType(getTypeMetadata());
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
        return Utils.map(getRawFlow().getParameters(), p -> new ParameterRef(this, p));
    }

    @Override
    public Property getProperty() {
         return getRawFlow();
    }

    public TypeMetadata getTypeMetadata0() {
        return getRawFlow().getTypeMetadata(getAllTypeArguments());
    }

    public boolean isSetter() {
        return getRawFlow().isSetter();
    }

    public boolean isGetter() {
        return getRawFlow().isGetter();
    }

    public String getPropertyName() {
        return getRawFlow().getPropertyName();
    }

    public boolean isNative() {
        return getRawFlow().isNative();
    }

    public boolean isPrivate() {
        return getRawFlow().isPrivate();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethodRef(this);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        declaringType.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        declaringType.forEachReference(action);
    }
}
