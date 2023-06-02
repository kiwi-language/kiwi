package tech.metavm.transpile.ir;

import tech.metavm.transpile.TypeRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneralTypeSet extends IRType implements TypeSet {

    private final List<TypeRange> ranges;

    public GeneralTypeSet(List<TypeRange> ranges) {
        super(null);
        this.ranges = new ArrayList<>(ranges);
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

    @Override
    public TypeSet intersect(TypeSet that) {
        return null;
    }

    @Override
    public TypeSet union(TypeSet that) {
        return null;
    }

    @Override
    public List<TypeRange> ranges() {
        return ranges;
    }
}
