package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType
@Slf4j
public class MethodRef extends FlowRef implements PropertyRef {

    public static MethodRef create(MethodRefDTO methodRefDTO, IEntityContext context) {
        return new MethodRef(
                (ClassType) TypeParser.parseType(methodRefDTO.declaringType(), context),
                context.getMethod(methodRefDTO.rawFlowId()),
                NncUtils.map(methodRefDTO.typeArguments(), t -> TypeParser.parseType(t, context))
        );
    }

    private final ClassType declaringType;

    public MethodRef(ClassType declaringType, @NotNull Method rawFlow, List<Type> typeArguments) {
        super(rawFlow, typeArguments);
        this.declaringType = declaringType;
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    @Override
    public Method getRawFlow() {
        return (Method) super.getRawFlow();
    }

    @Override
    public Method resolve() {
        if(resolved != null) {
            return (Method) resolved;
        }
        var klass = declaringType.resolve();
        var partialResolved = klass.findMethod(m -> m.getEffectiveVerticalTemplate() == getRawFlow());
        if (partialResolved == null) {
            log.info("all methods in klass");
            klass.forEachMethod(m -> log.info(m.getQualifiedSignature()));
            throw new InternalException("fail to resolve methodRef: " + this);
        }
        var r =  partialResolved.getParameterized(getTypeArguments());
        resolved = r;
        return r;
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

    public MethodRefDTO toDTO(SerializeContext serializeContext) {
        return toDTO(serializeContext, null);
    }

    public MethodRefDTO toDTO(SerializeContext serializeContext, Function<TypeDef, String> getTypeDefId) {
        return new MethodRefDTO(
                declaringType.toExpression(serializeContext, getTypeDefId),
                serializeContext.getStringId(getRawFlow()),
                NncUtils.map(typeArguments, t -> t.toExpression(serializeContext, getTypeDefId))
        );
    }

    @Override
    protected String toString0() {
        return "{\"declaringType\": \"" + declaringType + "\", \"rawMethod\": \"" + getRawFlow().getSignatureString() + "\"}";
    }

    public static MethodRef read(InstanceInput input, TypeDefProvider typeDefProvider) {
       var classType = ClassType.read(input, typeDefProvider);
       var rawMethod = (Method) typeDefProvider.getTypeDef(input.readId());
       var typeArgsCount = input.readInt();
       var typeArgs = new ArrayList<Type>(typeArgsCount);
       for (int i = 0; i < typeArgsCount; i++) {
           typeArgs.add(Type.readType(input, typeDefProvider));
       }
       return new MethodRef(classType, rawMethod, typeArgs);
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.METHOD_REF);
        declaringType.write(output);
        output.writeId(getRawFlow().getId());
        output.writeInt(getTypeArguments().size());
        for (Type typeArgument : getTypeArguments()) {
            typeArgument.write(output);
        }
    }

    @Override
    public GenericDeclarationRefKey toGenericDeclarationKey(Function<TypeDef, Id> getTypeDefId) {
        try(var serContext = SerializeContext.enter()) {
            return toDTO(serContext, typeDef -> Constants.addIdPrefix(getTypeDefId.apply(typeDef).toString()));
        }
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        return declaringType.toExpression(serializeContext, getTypeDefExpr) + "::"
                + Constants.addIdPrefix(serializeContext.getStringId(getRawFlow()))
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
