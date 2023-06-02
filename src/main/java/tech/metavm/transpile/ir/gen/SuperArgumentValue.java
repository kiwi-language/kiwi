package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.NncUtils;

public record SuperArgumentValue(
        Value value,
        TypeVariable<IRClass> typeParam,
        boolean isUpperBound
) implements Value {

    public IRType get() {
        var types = PTypeUtil.pullUp(value.get(), typeParam);
        if(types.isEmpty()) {
            return isUpperBound ? ObjectClass.getInstance() : IRAnyType.getInstance();
        }
        if(isUpperBound) {
            return TypeIntersection.of(NncUtils.map(types, IRType::getUpperBound));
        }
        else {
            return TypeUnion.of(NncUtils.map(types, IRType::getLowerBound));
        }
    }


}
