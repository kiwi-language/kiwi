package org.metavm.flow;

import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public interface Callable {

    Type getReturnType();

    List<Parameter> getParameters();

    default Parameter getParameter(int index) {
        return getParameters().get(index);
    }

    default @Nullable Parameter findParameter(Predicate<Parameter> predicate) {
        return NncUtils.find(getParameters(), predicate);
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
