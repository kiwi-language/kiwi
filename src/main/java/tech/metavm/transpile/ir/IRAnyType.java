package tech.metavm.transpile.ir;

import java.util.List;
import java.util.Map;

public class IRAnyType extends IRType{

    private static final IRAnyType INSTANCE = new IRAnyType();

    public static IRAnyType getInstance() {
        return INSTANCE;
    }

    private IRAnyType() {
        super("any");
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        return that instanceof IRAnyType;
    }

    @Override
    public List<IRType> getReferences() {
        return List.of();
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return this;
    }
}
