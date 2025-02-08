package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;

@Entity(ephemeral = true)
public class DummyCallable implements Callable, ConstantScope {

    public static final DummyCallable INSTANCE = new DummyCallable();

    private DummyCallable() {
    }

    @Override
    public Type getReturnType() {
        return null;
    }

    @Override
    public List<Parameter> getParameters() {
        return null;
    }

    @Override
    public int getInputCount() {
        return 0;
    }

    @Override
    public void setParameters(List<Parameter> parameters) {

    }

    @Override
    public Parameter getParameterByName(String name) {
        return null;
    }

    @Override
    public FunctionType getFunctionType() {
        return null;
    }

    @Override
    public CallableRef getRef() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Code getCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstantPool getConstantPool() {
        return new ConstantPool(this) {

            public int getEntityTag() {
                throw new UnsupportedOperationException();
            }

            @Generated
            public static void visitBody(StreamVisitor visitor) {
                ConstantPool.visitBody(visitor);
            }

            @SuppressWarnings("unused")
            private static org.metavm.object.type.Klass __klass__;

            @Override
            public int addValue(Value value) {
                return 0;
            }
        };
    }

}
