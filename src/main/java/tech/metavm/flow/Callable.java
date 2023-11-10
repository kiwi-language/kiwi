package tech.metavm.flow;

import tech.metavm.object.meta.FunctionType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public interface Callable {

    Type getReturnType();

    List<Parameter> getParameters();

    void setParameters(List<Parameter> parameters);

    Parameter getParameterByName(String name);

    default Parameter getParameterByTemplate(Parameter template) {
        return NncUtils.findRequired(getParameters(), p -> p.getTemplate() == template);
    }

    FunctionType getFunctionType();

    void setFunctionType(FunctionType functionType);

    default List<Type> getParameterTypes() {
        return NncUtils.map(getParameters(), Parameter::getType);
    }
}
