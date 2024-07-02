package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.ParameterRefDTO;
import org.metavm.util.NncUtils;

import java.util.Objects;

@EntityType
public class ParameterRef  extends ValueElement implements Reference {

    private final CallableRef callableRef;
    private final Parameter rawParameter;
    @CopyIgnore
    private transient Parameter resolved;

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

    public Parameter resolve() {
        if(resolved != null)
            return resolved;
        return resolved = NncUtils.findRequired(callableRef.resolve().getParameters(), p -> p.getUltimateTemplate() == rawParameter);
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

    public ParameterRefDTO toDTO(SerializeContext serializeContext) {
        return new ParameterRefDTO(
                callableRef.toDTO(serializeContext),
                serializeContext.getStringId(rawParameter)
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameterRef(this);
    }

}
