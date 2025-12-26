package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;

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
    public void addParameter(Parameter parameter) {

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

            @Override
            public int addValue(Value value) {
                return 0;
            }
        };
    }

    @Override
    public List<TypeVariable> getAllTypeParameters() {
        return List.of();
    }
}
