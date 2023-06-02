package tech.metavm.transpile.ir.gen;

import org.jetbrains.annotations.Nullable;
import tech.metavm.transpile.ir.IRAnyType;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.IRUtil;

import java.util.List;
import java.util.Map;

public abstract class XType extends IRType {

    public XType(@Nullable String name) {
        super(name);
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        return false;
    }

    @Override
    public IRType getLowerBound() {
        return IRAnyType.getInstance();
    }

    @Override
    public IRType getUpperBound() {
        return IRUtil.getObjectClass();
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
