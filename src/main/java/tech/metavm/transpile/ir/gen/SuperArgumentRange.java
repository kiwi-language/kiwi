package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.NncUtils;

public record SuperArgumentRange(
    Value value,
    TypeVariable<IRClass> typeParameter
) implements Range {

    @Override
    public Value getLowerBound() {
        return () -> {
            var types = PTypeUtil.pullUp(value.get(), typeParameter);
            if (types.isEmpty()) {
                return IRAnyType.getInstance();
            }
            return TypeUnion.of(NncUtils.map(types, IRType::getLowerBound));
        };
    }

    @Override
    public Value getUpperBound() {
        return () -> {
            var types = PTypeUtil.pullUp(value.get(), typeParameter);
            if (types.isEmpty()) {
                return ObjectClass.getInstance();
            }
            return TypeIntersection.of(NncUtils.map(types, IRType::getUpperBound));
        };
    }

}
