package tech.metavm.transpile.ir;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SolvingType extends IRType {

    private final IRType min;
    private final IRType max;

    public SolvingType(@Nullable String name, IRType min, IRType max) {
        super(name);
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean isAssignableFrom0(IRType that) {
        return false;
    }

    @Override
    public IRType getUpperBound() {
        return min;
    }

    @Override
    public IRType getLowerBound() {
        return max;
    }

    @Override
    public List<IRType> getReferences() {
        return List.of(min, max);
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return new SolvingType(getName(), referenceMap.get(min), referenceMap.get(max));
    }

}
