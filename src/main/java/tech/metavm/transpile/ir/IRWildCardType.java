package tech.metavm.transpile.ir;

import tech.metavm.transpile.TypeRange;
import tech.metavm.util.NncUtils;

import java.util.List;

public class IRWildCardType extends TypeRange {

    public static IRWildCardType asterisk() {
        return new IRWildCardType(
                List.of(IRAnyType.getInstance()),
                List.of(IRUtil.getObjectClass())
        );
    }

    public static IRWildCardType extensionOf(IRType type) {
        return new IRWildCardType(List.of(), List.of(type));
    }

    public static IRWildCardType superOf(IRType type) {
        return new IRWildCardType(List.of(type), List.of());
    }

    private static String getName(List<IRType> upperBounds, List<IRType> lowerBounds) {
        String s = "?";
        if(NncUtils.isNotEmpty(upperBounds)) {
            s += " extends " + NncUtils.join(
                    upperBounds, IRType::getName, ", "
            );
        }
        if(NncUtils.isNotEmpty(lowerBounds)) {
            s += " super " + NncUtils.join(
                    lowerBounds, IRType::getName, ", "
            );
        }
        return s;
    }

    public IRWildCardType(List<IRType> lowerBounds, List<IRType> upperBounds) {
        super(getName(upperBounds, lowerBounds), upperBounds, lowerBounds);
    }

    @Override
    public boolean isTypeAssignableFrom(IRType type) {
        return getUpperBound().isAssignableFrom(type.getUpperBound())
                && type.getLowerBound().isAssignableFrom(getLowerBound());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IRWildCardType that) {
            return getLowerBound().equals(that.getLowerBound())
                    && getUpperBound().equals(that.getUpperBound());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getLowerBound().hashCode() ^ getUpperBound().hashCode();
    }
}
