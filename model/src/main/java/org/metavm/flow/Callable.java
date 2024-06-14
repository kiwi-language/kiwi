package org.metavm.flow;

import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.List;

public interface Callable {

    Type getReturnType();

    List<Parameter> getParameters();

    default Parameter getParameter(int index) {
        return getParameters().get(index);
    }

    void setParameters(List<Parameter> parameters);

    default Parameter getParameterByName(String name) {
        return NncUtils.find(getParameters(), p -> p.getName().equals(name));
    }

    FunctionType getFunctionType();

    default List<Type> getParameterTypes() {
        return NncUtils.map(getParameters(), Parameter::getType);
    }

    CallableRef getRef();

}
