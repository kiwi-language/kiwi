package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.FlowRefDTO;
import tech.metavm.flow.rest.FunctionRefDTO;
import tech.metavm.flow.rest.MethodRefDTO;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("FlowRef")
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
    @ChildEntity("typeArguments")
    private final ChildArray<Type> typeArguments = addChild(new ChildArray<>(Type.class), "typeArguments");
    protected transient Flow resolved;

    public FlowRef(Flow rawFlow, List<Type> typeArguments) {
        this.rawFlow = rawFlow;
        typeArguments.forEach(t -> this.typeArguments.addChild(t.copy()));
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

    public abstract FlowRef copy();

}
