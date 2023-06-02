package tech.metavm.transpile.ir;

import java.util.List;

public record DiamondType(
        IRType ownerType,
        IRClass rawClass
) implements TypeLike {

    public PType resolve(List<IRType> typeArguments) {
        return new PType(ownerType, rawClass, typeArguments);
    }

}
