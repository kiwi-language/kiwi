package org.metavm.flow;

import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueArray;
import org.metavm.flow.rest.FlowRefDTO;
import org.metavm.flow.rest.FunctionRefDTO;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public abstract class FlowRef extends CallableRef {

    public static FlowRef create(FlowRefDTO flowRefDTO, IEntityContext context) {
        if(flowRefDTO instanceof MethodRefDTO methodRefDTO)
            return MethodRef.create(methodRefDTO, context);
        else if(flowRefDTO instanceof FunctionRefDTO functionRefDTO)
            return FunctionRef.create(functionRefDTO, context);
        else
            throw new IllegalArgumentException("Invalid FlowRefDTO: " + flowRefDTO);
    }

    private final Flow rawFlow;
    private final ValueArray<Type> typeArguments;
    protected transient Flow resolved;

    public FlowRef(Flow rawFlow, List<Type> typeArguments) {
        this.rawFlow = rawFlow;
        this.typeArguments = new ValueArray<>(Type.class, typeArguments);
    }

    public Flow getRawFlow() {
        return rawFlow;
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
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

    public abstract FlowRefDTO toDTO(SerializeContext serializeContext);

}
