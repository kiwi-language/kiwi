package tech.metavm.transpile;

import org.jetbrains.annotations.Nullable;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class TypeRange extends IRType implements TypeSet {

    public static TypeRange superOf(IRType type) {
        return new IRWildCardType(List.of(type), List.of());
    }

    public static IRType minRange() {
        return between(ObjectClass.getInstance(), IRAnyType.getInstance());
    }

    public static IRType maxRange() {
        return between(IRAnyType.getInstance(), ObjectClass.getInstance());
    }

    public static TypeRange extensionOf(IRType type) {
        return new IRWildCardType(List.of(), List.of(type.getLowerBound()));
    }

    public static IRType between(IRType lowerBound, IRType upperBound) {
        lowerBound = lowerBound.getUpperBound();
        upperBound = upperBound.getLowerBound();
        if(lowerBound.equals(upperBound)) {
            return lowerBound;
        }
        if(upperBound.isAssignableFrom(lowerBound)) {
            return new IRWildCardType(List.of(lowerBound), List.of(upperBound));
        }
        throw new InternalException("Invalid range [" + lowerBound + ", " + upperBound + "]");
    }

    public static IRType intersect(IRType type1, IRType type2) {
        IRType lowerBound = IRUtil.upperBound(type1.getLowerBound(), type2.getLowerBound());
        IRType upperBound = IRUtil.lowerBound(type1.getUpperBound(), type2.getUpperBound());
        return between(lowerBound, upperBound);
    }

    private final List<IRType> upperBounds;
    private final List<IRType> lowerBounds;

    public TypeRange(@Nullable String name, List<IRType> upperBounds, List<IRType> lowerBounds) {
        super(name);
        this.upperBounds = new ArrayList<>(upperBounds);
        this.lowerBounds = new ArrayList<>(lowerBounds);
    }

    @Override
    public List<TypeRange> ranges() {
        return List.of(this);
    }

    public boolean contains(IRType type) {
        return getUpperBound().isAssignableFrom(type.getUpperBound())
                && type.getLowerBound().isAssignableFrom(getLowerBound());
    }

    @Override
    public boolean isAssignableFrom0(IRType that) {
        throw new UnsupportedOperationException();
    }

    public List<IRType> getUpperBounds() {
        return Collections.unmodifiableList(upperBounds);
    }

    public List<IRType> getLowerBounds() {
        return Collections.unmodifiableList(lowerBounds);
    }

    @Override
    public IRType getLowerBound() {
        return lowerBounds.isEmpty() ? IRAnyType.getInstance() : (
                lowerBounds.size() == 1 ? lowerBounds.get(0) : new TypeIntersection(lowerBounds)
        );
    }

    @Override
    public List<IRType> getReferences() {
        return List.of(getLowerBound(), getUpperBound());
    }

    @Override
    public IRType cloneWithReferences(Map<IRType, IRType> referenceMap) {
        return null;
    }

    @Override
    public IRType getUpperBound() {
        return upperBounds.size() == 1 ? upperBounds.get(0) : new TypeIntersection(upperBounds);
    }

    @Override
    public boolean isWithinRange(IRType type) {
        return NncUtils.allMatch(upperBounds, u -> u.isAssignableFrom(type))
                && NncUtils.allMatch(lowerBounds, type::isAssignableFrom);
    }

    public boolean canMergeWith(TypeRange that) {
        return that.isTypeAssignableFrom(getLowerBound()) || that.isTypeAssignableFrom(that.getUpperBound()) ||
                isTypeAssignableFrom(that.getLowerBound()) || isTypeAssignableFrom(that.getUpperBound());
    }

    public boolean overlapsWith(TypeRange that) {
        if(!canMergeWith(that)) {
            return false;
        }
        return !that.getUpperBound().equals(getLowerBound()) && !that.getLowerBound().equals(getUpperBound());
    }

    public TypeRange intersect(TypeRange that) {
        if(overlapsWith(that)) {
            return new IRWildCardType(
                    List.of(IRUtil.upperBound(getLowerBound(), that.getLowerBound())),
                    List.of(IRUtil.lowerBound(getUpperBound(), that.getUpperBound()))
            );
        }
        throw new InternalException("Can not intersect " + this + " with " + that);
    }

    public TypeRange mergeWith(TypeRange that) {
        if(canMergeWith(that)) {
            if(that.getUpperBound().isAssignableFrom(getUpperBound())) {
                return (TypeRange) between(getLowerBound(), that.getUpperBound());
            }
            else {
                return (TypeRange) between(that.getLowerBound(), getUpperBound());
            }
        }
        throw new InternalException("Can not merge " + this + " with " + that);
    }

}
