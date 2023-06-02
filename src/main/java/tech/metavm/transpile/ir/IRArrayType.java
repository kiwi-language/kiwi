package tech.metavm.transpile.ir;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IRArrayType extends IRType {

    private final IRType elementType;

    public IRArrayType(IRType elementType) {
        super(elementType.getName() + "[]");
        this.elementType = elementType;
    }

    public IRType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IRArrayType that) {
            return elementType.equals(that.elementType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(IRArrayType.class, elementType.hashCode());
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        return that instanceof IRArrayType thatArray && elementType.isAssignableFrom(thatArray.elementType);
    }

    @Override
    public List<IRType> getReferences() {
        return List.of(elementType);
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return new IRArrayType(referenceMap.get(elementType));
    }
}
