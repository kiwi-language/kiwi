package tech.metavm.object.type;

import java.util.List;

public interface FunctionTypeProvider {

    FunctionType getFunctionType(List<Type> parameterTypes, Type returnType, Long tmpId);

    default FunctionType getFunctionType(List<Type> parameterTypes, Type returnType) {
        return getFunctionType(parameterTypes, returnType, null);
    }

}