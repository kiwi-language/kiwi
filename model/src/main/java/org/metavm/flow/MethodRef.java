package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.CopyIgnore;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.MethodRefKey;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.*;
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

    public static MethodRef createMethodRef(MethodRefKey methodRefKey, TypeDefProvider typeDefProvider) {
        var classType = (ClassType) TypeParser.parseType(methodRefKey.declaringType(), typeDefProvider);
        var klass = classType.getKlass();
        var methodId = Id.parse(methodRefKey.rawFlowId());
        var method = Objects.requireNonNull(klass.findSelfMethod(m -> m.idEquals(methodId)),
                () -> "Cannot find method with ID " + methodId + " in klass " + klass.getTypeDesc());
        return new MethodRef(classType, method,
                NncUtils.map(methodRefKey.typeArguments(), t -> TypeParser.parseType(t, typeDefProvider)));
    }

    private final ClassType declaringType;
    @CopyIgnore
    protected transient Method partialResolved;


    public MethodRef(ClassType declaringType, @NotNull Method rawFlow, List<Type> typeArguments) {
        super(rawFlow, typeArguments);
        this.declaringType = declaringType;
        if(declaringType.getKlass() == DummyKlass.INSTANCE)
            throw new RuntimeException("Creating MethodRef with DummyKlass");
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    @Override
    public Method getRawFlow() {
        return (Method) super.getRawFlow();
    }

    private Method partialResolve() {
        if(partialResolved != null)
            return partialResolved;
        var klass = declaringType.resolve();
        partialResolved = klass.findMethod(m -> m.getEffectiveVerticalTemplate() == getRawFlow());
        if (partialResolved == null) {
            klass.forEachMethod(m -> log.info(m.getQualifiedSignature()));
            throw new InternalException("fail to resolve methodRef: " + this);
        }
        return partialResolved;
    }

    @Override
    public Method resolve() {
        if(resolved != null) {
            return (Method) resolved;
        }
        var r =  isParameterized() ? partialResolve().getParameterized(getTypeArguments()) : partialResolve();
        resolved = r;
        return r;
    }

    @Override
    public List<Type> getTypeArguments() {
        return typeArguments.isEmpty() ? partialResolve().getTypeArguments() : typeArguments.toList();
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
        return "{\"declaringType\": \"" + declaringType + "\", \"rawMethod\": \"" + getRawFlow().getSignatureString() + "\"}";
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

}
