package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.ParameterRefDTO;
import tech.metavm.util.NncUtils;

import java.util.Objects;

@EntityType("ParameterRef")
public class ParameterRef  extends ValueElement implements Reference {

    @ChildEntity("callableRef")
    private final CallableRef callableRef;
    private final Parameter rawParameter;
    private transient Parameter resolved;

    public ParameterRef(CallableRef callableRef, Parameter rawParameter) {
        this.callableRef = addChild(callableRef.copy(), "callableRef");
        this.rawParameter = rawParameter;
    }

    public CallableRef getCallableRef() {
        return callableRef;
    }

    public Parameter getRawParameter() {
        return rawParameter;
    }

    public Parameter resolve() {
        return NncUtils.find(callableRef.resolve().getParameters(), p -> p.getUltimateTemplate() == rawParameter);
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

    @Override
    public ParameterRef copy() {
        var copy = new ParameterRef(callableRef, rawParameter);
        copy.resolved = resolved;
        return copy;
    }
}
