package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.FlowRefDTO;
import org.metavm.flow.rest.FunctionRefDTO;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public abstract class FlowRef extends CallableRef implements GenericDeclarationRef {

    public static FlowRef create(FlowRefDTO flowRefDTO, EntityRepository context) {
        if(flowRefDTO instanceof MethodRefDTO methodRefDTO)
            return MethodRef.createMethodRef(methodRefDTO, context);
        else if(flowRefDTO instanceof FunctionRefDTO functionRefDTO)
            return FunctionRef.create(functionRefDTO, context);
        else
            throw new IllegalArgumentException("Invalid FlowRefDTO: " + flowRefDTO);
    }

    private final Flow rawFlow;
    protected final ValueArray<Type> typeArguments;
    @CopyIgnore
    protected transient Flow resolved;

    public FlowRef(Flow rawFlow, List<Type> typeArguments) {
        this.rawFlow = rawFlow;
        this.typeArguments =  new ValueArray<>(Type.class, typeArguments);
    }

    public Flow getRawFlow() {
        return rawFlow;
    }

    public List<Type> getTypeArguments() {
        return typeArguments.isEmpty() ? rawFlow.getTypeArguments() : typeArguments.toList();
    }

    public Flow resolve() {
       if(resolved != null) {
           return resolved;
       }
       return resolved = (typeArguments.isEmpty() ? rawFlow : rawFlow.getParameterized(typeArguments.toList()));
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FlowRef flowRef)) return false;
        return Objects.equals(rawFlow, flowRef.rawFlow) && Objects.equals(typeArguments, flowRef.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawFlow, typeArguments);
    }

    public FlowRefDTO toDTO() {
        try(var serContext = SerializeContext.enter()) {
            return toDTO(serContext);
        }
    }

    public abstract FlowRefDTO toDTO(SerializeContext serializeContext);

    public boolean isParameterized() {
        return !typeArguments.isEmpty();
    }
}
