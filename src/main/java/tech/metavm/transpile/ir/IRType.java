package tech.metavm.transpile.ir;

import tech.metavm.entity.NoProxy;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class IRType implements TypeLike {

    @Nullable
    private final String name;
    private IRArrayType arrayType;

    public IRType(@Nullable String name) {
        this.name = name;
    }

    public boolean isAssignableFrom(IRType that) {
        if(this instanceof TypeUnion union) {
            return NncUtils.anyMatch(union.getTypes(), t -> t.isAssignableFrom(that));
        }
        else if(this instanceof TypeIntersection intersection) {
            return NncUtils.allMatch(intersection.getTypes(), t -> t.isAssignableFrom(that));
        }
        else if(that instanceof TypeUnion union) {
            return NncUtils.allMatch(union.getTypes(), this::isAssignableFrom);
        }
        else if(that instanceof TypeIntersection intersection) {
            return NncUtils.anyMatch(intersection.getTypes(), this::isAssignableFrom);
        }
        else if(isRange()) {
            return getLowerBound().isAssignableFrom(that);
        }
        else if(that.isRange()) {
            return isAssignableFrom(that.getUpperBound());
        }
        else {
            return isAssignableFrom0(that);
        }
    }

    protected abstract boolean isAssignableFrom0(IRType that);

    public IRArrayType getArrayType() {
        if(arrayType == null) {
            arrayType = new IRArrayType(this);
        }
        return arrayType;
    }

    @Nullable
    @NoProxy
    public String getName() {
        return name;
    }

    @NoProxy
    public String getSimpleName() {
        if(name == null) {
            return null;
        }
        int lastDotIdx = name.lastIndexOf('.');
        if(lastDotIdx >= 0) {
            return name.substring(lastDotIdx + 1);
        }
        else {
            return name;
        }
    }

    public boolean isRange() {
        return !isAtomic();
    }

    public boolean isAtomic() {
        return getLowerBound().equals(getUpperBound());
    }

    public boolean isTypeAssignableFrom(IRType type) {
        return equals(type);
    }

    public boolean typeEquals(IRType type) {
        return isAssignableFrom(type) && type.isAssignableFrom(this);
    }

    public IRType getUpperBound() {
        return this;
    }

    public IRType getLowerBound() {
        return this;
    }

    public abstract List<IRType> getReferences();

    public abstract IRType cloneWithReferences(Map<IRType, IRType> referenceMap);

    public IRType substituteReferenceRecursively(Map<? extends IRType, ? extends IRType> substitutionMap) {
        if(substitutionMap.containsKey(this)) {
            return substitutionMap.get(this);
        }
        return cloneWithReferences(
                NncUtils.toMap(
                        getReferences(),
                        Function.identity(),
                        r -> r.substituteReferenceRecursively(substitutionMap)
                )
        );
    }

    public boolean isWithinRange(IRType type) {
        return type instanceof IRAnyType || type.equals(this);
    }

    public boolean isConstant() {
        return !isVariable();
    }

    public boolean isVariable() {
        if(this instanceof XType) {
            return true;
        }
        return NncUtils.anyMatch(getReferences(), IRType::isVariable);
    }

    public List<XType> getVariables() {
        List<XType> result = new ArrayList<>();
        extractVariables0(result);
        return result;
    }

    public IRType getVariableValue(XType variable) {
        throw new UnsupportedOperationException();
    }

    private void extractVariables0(List<XType> result) {
        if(this instanceof XType variable) {
            result.add(variable);
        }
        else {
            getReferences().forEach(r -> r.extractVariables0(result));
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
