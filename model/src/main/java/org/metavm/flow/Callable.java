package org.metavm.flow;

import org.metavm.api.JsonIgnore;
import org.metavm.object.type.ConstantPool;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public interface Callable {

    Type getReturnType();

    List<Parameter> getParameters();

    int getTypeIndex();

    @JsonIgnore
    int getInputCount();

    default Parameter getParameter(int index) {
        return getParameters().get(index);
    }

    default @Nullable Parameter findParameter(Predicate<Parameter> predicate) {
        return Utils.find(getParameters(), predicate);
    }

    void setParameters(List<Parameter> parameters);

    default Parameter getParameterByName(String name) {
        return Utils.find(getParameters(), p -> p.getName().equals(name));
    }

    FunctionType getFunctionType();

    default List<Type> getParameterTypes() {
        return getParameterTypes(getConstantPool());
    }

    default List<Type> getParameterTypes(TypeMetadata typeMetadata) {
        return Utils.map(getParameters(), p -> p.getType(typeMetadata));
    }

    CallableRef getRef();

    Code getCode();

    default int getMinLocals() {
        return getParameters().size();
    }

    ConstantPool getConstantPool();

}
