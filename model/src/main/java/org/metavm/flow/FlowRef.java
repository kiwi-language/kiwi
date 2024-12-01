package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.CopyIgnore;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.entity.ValueArray;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;

import java.util.List;
import java.util.Objects;

@EntityType
public abstract class FlowRef extends CallableRef implements GenericDeclarationRef {

    private final Flow rawFlow;
    protected final ValueArray<Type> typeArguments;
    @CopyIgnore
    protected transient Flow resolved;
    @CopyIgnore
    private transient TypeMetadata typeMetadata;

    public FlowRef(Flow rawFlow, List<? extends Type> typeArguments) {
        this.rawFlow = rawFlow;
        this.typeArguments =  new ValueArray<>(Type.class, typeArguments);
    }

    public Flow getRawFlow() {
        return rawFlow;
    }

    public List<Type> getTypeArguments() {
        return typeArguments.isEmpty() ? rawFlow.getDefaultTypeArguments() : typeArguments.toList();
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

    public boolean isParameterized() {
        return !typeArguments.isEmpty();
    }

    public List<Type> getParameterTypes() {
        return rawFlow.getParameterTypes(getTypeMetadata());
    }

    public Type getReturnType() {
        return rawFlow.getReturnType(getTypeMetadata());
    }

    public boolean matches(String name, List<Type> argumentTypes) {
        if (rawFlow.getName().equals(name)) {
            var parameters = rawFlow.getParameters();
            if (parameters.size() == argumentTypes.size()) {
                for (int i = 0; i < parameters.size(); i++) {
                    var paramType = parameters.get(i).getType(getTypeMetadata());
                    var argType = argumentTypes.get(i);
                    if (!paramType.isConvertibleFrom(argType))
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    public FunctionType getType() {
        return (FunctionType) getTypeMetadata().getType(rawFlow.getTypeIndex());
    }

    public FlowExecResult execute(ClassInstance boundSelf, List<? extends Value> arguments, CallContext callContext) {
//        logger.debug("Executing flow {}", this);
        return rawFlow.execute(boundSelf, arguments, this, callContext);
    }

    public String getQualifiedName() {
        return getRawFlow().getQualifiedName();
    }

    public abstract FlowRef getParameterized(List<? extends Type> typeArguments);

    @Override
    public TypeMetadata getTypeMetadata() {
        if(typeMetadata == null)
            typeMetadata = getTypeMetadata0();
        return typeMetadata;
    }

    protected abstract TypeMetadata getTypeMetadata0();

    public int getParameterCount() {
        return getRawFlow().getParameters().size();
    }

}
