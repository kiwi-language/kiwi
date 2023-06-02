package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionType extends IRType {

    private final List<IRType> parameterTypes;
    private final IRType returnType;

    private static String getName(List<IRType> parameterTypes, IRType returnType) {
        return "(" + NncUtils.join(parameterTypes, IRType::getName, ", ") +
                ")" + returnType.getName();
    }

    public FunctionType(List<IRType> parameterTypes, IRType returnType) {
        super(getName(parameterTypes, returnType));
        this.parameterTypes = new ArrayList<>(parameterTypes);
        this.returnType = returnType;
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        return false;
    }

    @Override
    public List<IRType> getReferences() {
        return null;
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return null;
    }

    public List<IRType> getParameterTypes() {
        return parameterTypes;
    }

    public IRType getReturnType() {
        return returnType;
    }
}
