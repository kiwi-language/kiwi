package tech.metavm.transpile.ir;

import java.util.List;
import java.util.Map;

public class NoneType extends IRType {

    private static final NoneType INSTANCE = new NoneType();

    public static NoneType getInstance() {
        return INSTANCE;
    }

    private NoneType() {
        super("None");
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
}
