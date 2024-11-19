package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.Attribute;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;

import javax.annotation.Nullable;
import java.util.List;

@EntityType(ephemeral = true)
public class DummyMethod extends Method {

    public static final DummyMethod INSTANCE = new DummyMethod();

    private DummyMethod() {
        super(
                null,
                DummyKlass.INSTANCE,
                "<unnamed>",
                false,
                true,
                false,
                false,
                List.of(),
                Types.getVoidType(),
                List.of(),
                List.of(),
                false,
                null,
                Access.PUBLIC,
                null,
                false,
                MetadataState.READY
        );
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlowExecResult execute(@Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Method createParameterized(List<? extends Type> typeArguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodRef getRef() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable) {
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
    }

    @Override
    public void addLambda(Lambda lambda) {
    }

    @Override
    public void addParameterized(Flow parameterized) {
    }

    @Override
    void addNode(Node node) {
    }

    @Override
    public void read(KlassInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(KlassOutput output) {
        throw new UnsupportedOperationException();
    }

    public ReadWriteArray<Attribute> getAttributeArray() {
        return attributes;
    }

}
