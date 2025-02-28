package org.metavm.object.instance.core;

import org.metavm.entity.ElementVisitor;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.CallableRef;
import org.metavm.flow.ClosureContext;
import org.metavm.flow.Code;
import org.metavm.flow.FlowExecResult;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.TypeMetadata;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class FunctionValue implements Value, CallableRef {

    public FunctionValue() {
    }

    public abstract FlowExecResult execute(List<? extends Value> arguments, CallContext callContext);

    @Override
    public abstract FunctionType getValueType();

//    public abstract Frame createFrame(FlowStack stack, List<Instance> arguments);

    @Override
    public FieldValue toFieldValueDTO() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public InstanceParam getParam() {
        return null;
    }

    @Override
    public Object toSearchConditionValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.writeLine("function");
    }

    @Override
    public Object toJson() {
        throw new UnsupportedOperationException();
    }

    public abstract Code getCode();

    public @Nullable Value getSelf() {
        return null;
    }

    public abstract TypeMetadata getTypeMetadata();

    public abstract ClosureContext getClosureContext(Value[] stack, int base);

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    public void forEachReference(Consumer<Reference> action) {
    }
}
