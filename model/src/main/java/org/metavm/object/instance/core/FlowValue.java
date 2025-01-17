package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class FlowValue extends FunctionValue {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final FlowRef flow;
    @Nullable
    private final ClassInstance boundSelf;

    public FlowValue(FlowRef flow, @Nullable ClassInstance boundSelf) {
        this.flow = flow;
        this.boundSelf = boundSelf;
    }

    @Override
    public FlowExecResult execute(List<? extends Value> arguments, CallContext callContext) {
        if(boundSelf != null)
            return flow.execute(boundSelf.getReference(), arguments, callContext);
        else
            return flow.execute(arguments.getFirst(), arguments.subList(1, arguments.size()), callContext);
    }

//    public Frame createFrame(FlowStack stack, List<Instance> arguments) {
//        var self = boundSelf != null ? boundSelf : arguments.getFirst();
//        var actualArgs = boundSelf != null ? arguments : arguments.subList(1, arguments.size());
//        return flow.isNative() ? new NativeFrame(flow, self, arguments) :
//                new MetaFrame(flow, self, actualArgs, stack);
//    }

    @Override
    public void writeInstance(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public ClassInstance getBoundSelf() {
        return boundSelf;
    }

    public FlowRef getFlow() {
        return flow;
    }

    @Override
    public Code getCode() {
        return flow.getRawFlow().getCode();
    }

    @Nullable
    @Override
    public Value getSelf() {
        return boundSelf != null ? boundSelf.getReference() : null;
    }

    @Override
    public TypeMetadata getTypeMetadata() {
        return flow.getTypeMetadata();
    }

    @Override
    public ClosureContext getClosureContext(Value[] stack, int base) {
        return boundSelf != null ? boundSelf.getClosureContext() :
                (flow instanceof MethodRef m && !m.isStatic() ?
                        stack[base].getClosureContext() : null
                );
    }

    @Override
    public FunctionType getValueType() {
        if (flow instanceof MethodRef method && !method.isStatic() && boundSelf == null)
            return method.getStaticType();
        else
            return flow.getPropertyType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        flow.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        flow.forEachReference(action);
    }
}
