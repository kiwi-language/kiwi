package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRClass;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.PType;
import tech.metavm.transpile.ir.TypeUnion;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public record ExtensionRawNode(INode node) implements VirtualNode {

    @Override
    public IRType get() {
        var v = node.get();
        if(v instanceof TypeUnion union) {
            return TypeUnion.of(NncUtils.map(union.getTypes(), this::getRawTypeFromAtomic));
        }
        else {
            return getRawTypeFromAtomic(v);
        }
    }

    @Override
    public IRType solve() {
        node.solve();
        return get();
    }

    private IRType getRawTypeFromAtomic(IRType type) {
        return switch (type) {
            case PType pType -> pType.getRawType();
            case IRClass k -> k;
            default -> throw new InternalException("Invalid type " + type);
        };
    }

}
