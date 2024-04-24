package tech.metavm.flow;

import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.Type;

import java.util.List;

public class DummyCallable implements Callable {

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

}
