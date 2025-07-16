package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.CopyIgnore;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.entity.Writable;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public abstract class FlowRef implements GenericDeclarationRef, Writable, CallableRef {

    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
//    public Flow rawFlow;
    protected final Reference flowReference;
    protected final List<Type> typeArguments;
    @CopyIgnore
    private transient TypeMetadata typeMetadata;

    public FlowRef(Flow rawFlow, List<? extends Type> typeArguments) {
        this(rawFlow.getReference(), typeArguments);
//        this.rawFlow = rawFlow;
    }

    protected FlowRef(Reference flowReference, List<? extends Type> typeArguments) {
        this.flowReference = flowReference;
        this.typeArguments =  new ArrayList<>(typeArguments);
    }

    public Flow getRawFlow() {
        return (Flow) flowReference.get();
    }

    public List<Type> getTypeArguments() {
        return typeArguments.isEmpty() ? getRawFlow().getDefaultTypeArguments() : Collections.unmodifiableList(typeArguments);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FlowRef flowRef)) return false;
        return Objects.equals(flowReference, flowRef.flowReference) && Objects.equals(typeArguments, flowRef.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowReference, typeArguments);
    }

    public boolean isParameterized() {
        return !typeArguments.isEmpty();
    }

    public List<Type> getParameterTypes() {
        return getRawFlow().getParameterTypes(getTypeMetadata());
    }

    public Type getReturnType() {
        return getRawFlow().getReturnType(getTypeMetadata());
    }

    public boolean matches(String name, List<Type> argumentTypes) {
        if (getRawFlow().getName().equals(name)) {
            var parameters = getRawFlow().getParameters();
            if (parameters.size() == argumentTypes.size()) {
                for (int i = 0; i < parameters.size(); i++) {
                    var paramType = parameters.get(i).getType(getTypeMetadata());
                    var argType = argumentTypes.get(i);
                    if (!paramType.isAssignableFrom(argType))
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    public FunctionType getPropertyType() {
        return getRawFlow().getType(getTypeMetadata());
    }

    public FlowExecResult execute(Value boundSelf, List<? extends Value> arguments, CallContext callContext) {
//        logger.debug("Executing flow {}", this);
        return getRawFlow().execute(boundSelf, arguments, this, callContext);
    }

    public String getQualifiedName() {
        return getRawFlow().getQualifiedName();
    }

    public abstract FlowRef getParameterized(List<? extends Type> typeArguments);

    @Override
    public TypeMetadata getTypeMetadata() {
        if(typeMetadata == null)
            typeMetadata = getTypeMetadata0();
        else
            typeMetadata.ensureUptodate();
        return typeMetadata;
    }

    protected abstract TypeMetadata getTypeMetadata0();

    public int getParameterCount() {
        return getRawFlow().getParameters().size();
    }

    @Override
    public Code getCode() {
        return getRawFlow().getCode();
    }

    @Override
    public FlowRef getFlow() {
        return this;
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        typeArguments.forEach(arg -> arg.accept(visitor));
    }

    public void forEachReference(Consumer<Reference> action) {
        action.accept(flowReference);
        for (var typeArguments_ : typeArguments) typeArguments_.forEachReference(action);
    }
}
