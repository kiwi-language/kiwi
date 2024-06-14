package org.metavm.flow;

import org.metavm.entity.*;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeParser;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType
public class MethodRef extends FlowRef implements PropertyRef {

    public static MethodRef create(MethodRefDTO methodRefDTO, IEntityContext context) {
        return new MethodRef(
                (ClassType) TypeParser.parseType(methodRefDTO.declaringType(), context),
                context.getMethod(methodRefDTO.rawFlowId()),
                NncUtils.map(methodRefDTO.typeArguments(), t -> TypeParser.parseType(t, context))
        );
    }

    private final ClassType declaringType;

    public MethodRef(ClassType declaringType, Method rawFlow, List<Type> typeArguments) {
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
            logger.info("all methods in klass");
            klass.forEachMethod(m -> logger.info(m.getQualifiedName()));
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
        return new MethodRefDTO(
                declaringType.toExpression(serializeContext),
                serializeContext.getStringId(getRawFlow()),
                NncUtils.map(getTypeArguments(), t -> t.toExpression(serializeContext))
        );
    }

    @Override
    protected String toString0() {
        return "{\"declaringType\": \"" + declaringType + "\", \"rawMethod\": \"" + getRawFlow().getQualifiedName() + "\"}";
    }
}
