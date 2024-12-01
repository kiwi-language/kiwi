package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.Reference;
import org.metavm.entity.ValueElement;
import org.metavm.object.type.Type;

import java.util.Objects;

@EntityType
public class ParameterRef  extends ValueElement implements Reference {

    private final CallableRef callableRef;
    private final Parameter rawParameter;

    public ParameterRef(CallableRef callableRef, Parameter rawParameter) {
        this.callableRef = callableRef;
        this.rawParameter = rawParameter;
    }

    public CallableRef getCallableRef() {
        return callableRef;
    }

    public Parameter getRawParameter() {
        return rawParameter;
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParameterRef that)) return false;
        return Objects.equals(callableRef, that.callableRef) && Objects.equals(rawParameter, that.rawParameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callableRef, rawParameter);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameterRef(this);
    }

    public String getName() {
        return rawParameter.getName();
    }

    public Type getType() {
        return callableRef.getTypeMetadata().getType(rawParameter.getTypeIndex());
    }

}
