package tech.metavm.transpile.ir;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CaptureType extends IRType {

    private IRWildCardType wildCardType;

    public CaptureType(IRWildCardType wildCardType) {
        super("capture of " + wildCardType.getName());
        this.wildCardType = wildCardType;
    }


    @Override
    public IRType getLowerBound() {
        return wildCardType.getLowerBound();
    }

    @Override
    public IRType getUpperBound() {
        return wildCardType.getUpperBound();
    }

    @Override
    protected boolean isAssignableFrom0(IRType that) {
        return getLowerBound().isAssignableFrom0(that);
    }

    @Override
    public List<IRType> getReferences() {
        return List.of(wildCardType);
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return new CaptureType((IRWildCardType) referenceMap.get(wildCardType));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptureType that = (CaptureType) o;
        return Objects.equals(wildCardType, that.wildCardType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wildCardType);
    }
}
