package tech.metavm.flow;

import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

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

    default Parameter getParameterByTemplate(Parameter template) {
        return NncUtils.findRequired(getParameters(), p -> p.getTemplate() == template);
    }

    FunctionType getFunctionType();

    void setFunctionType(FunctionType functionType);

    default List<Type> getParameterTypes() {
        return NncUtils.map(getParameters(), Parameter::getType);
    }
}
