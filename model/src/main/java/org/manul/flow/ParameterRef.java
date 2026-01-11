package org.manul.flow;

import lombok.Getter;
import org.manul.api.Entity;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;

import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class ParameterRef implements org.manul.entity.Reference {

    @Getter
    private final CallableRef callableRef;
    private final org.manul.object.instance.core.Reference parameterReference;
    @Getter
    private Parameter rawParameter;

    public ParameterRef(CallableRef callableRef, Parameter rawParameter) {
        this(callableRef, rawParameter.getReference());
        this.rawParameter = rawParameter;
    }

    public ParameterRef(CallableRef callableRef, org.manul.object.instance.core.Reference parameterReference) {
        this.callableRef = callableRef;
        this.parameterReference = parameterReference;
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
