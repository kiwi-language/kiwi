package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;

import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class ParameterRef implements org.metavm.entity.Reference {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final CallableRef callableRef;
    private final org.metavm.object.instance.core.Reference parameterReference;
    private Parameter rawParameter;

    public ParameterRef(CallableRef callableRef, Parameter rawParameter) {
        this(callableRef, rawParameter.getReference());
        this.rawParameter = rawParameter;
    }

    public ParameterRef(CallableRef callableRef, org.metavm.object.instance.core.Reference parameterReference) {
        this.callableRef = callableRef;
        this.parameterReference = parameterReference;
    }

    public CallableRef getCallableRef() {
        return callableRef;
    }

    public Parameter getRawParameter() {
        return rawParameter;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParameterRef that)) return false;
        return Objects.equals(callableRef, that.callableRef) && Objects.equals(rawParameter, that.rawParameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callableRef, rawParameter);
    }

    public void write(MvOutput output) {
        callableRef.write(output);
        output.writeReference(parameterReference);
    }

    public static ParameterRef read(MvInput input) {
        return new ParameterRef((CallableRef) input.readValue(), input.readReference());
    }

    public String getName() {
        return rawParameter.getName();
    }

    public Type getType() {
        return callableRef.getTypeMetadata().getType(rawParameter.getTypeIndex());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameterRef(this);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        callableRef.accept(visitor);
        rawParameter.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        callableRef.forEachReference(action);
        action.accept(parameterReference);
        action.accept(rawParameter.getReference());
    }
}
