package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public record SuperOwnerValue(
        Value value,
        IRClass ownerClass
) implements Value {

    public IRType get() {
        var v = value.get();
        if(v instanceof TypeIntersection intersection) {
            return TypeIntersection.of(
                    NncUtils.map(
                            intersection.getTypes(),
                            this::getOwnerFromAtomicType
                    )
            );
        }
        else if(v instanceof TypeUnion) {
            throw new InternalException("Super type of PType can not be a type union");
        }
        else {
            return getOwnerFromAtomicType(v);
        }
    }

    private IRType getOwnerFromAtomicType(IRType type) {
        if(type instanceof PType pType) {
            if(pType.getOwnerType() != null
                    && IRUtil.getRawClass(pType.getOwnerType()).isAssignableFrom(ownerClass)) {
                return pType.getOwnerType();
            }
            else {
                return ObjectClass.getInstance();
            }
        }
        else {
            return ObjectClass.getInstance();
        }
    }


}
