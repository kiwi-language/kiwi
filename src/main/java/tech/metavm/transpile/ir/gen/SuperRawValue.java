package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRClass;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.PType;
import tech.metavm.transpile.ir.TypeIntersection;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public record SuperRawValue(
    Value value
) implements Value {

    @Override
    public IRType get() {
        var v = value.get();
        if(v instanceof TypeIntersection intersection) {
            return TypeIntersection.of(
                    NncUtils.map(intersection.getTypes(), this::getRawTypeFromAtomic)
            );
        }
        else {
            return getRawTypeFromAtomic(v);
        }
    }

    private IRType getRawTypeFromAtomic(IRType type) {
        return switch (type) {
            case PType pType -> pType.getRawType();
            case IRClass k -> k;
            default -> throw new InternalException("Invalid type " + type);
        };
    }
}
