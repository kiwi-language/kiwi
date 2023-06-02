package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.PType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

public record ExtensionOwnerNode(
        INode node
) implements VirtualNode {

    @Override
    public IRType get() {
        var v = node.get();
        if(v instanceof PType pType) {
            return NncUtils.requireNonNull(pType.getOwnerType());
        }
        else {
            throw new InternalException("The min value of a PType extension must also be a PType ");
        }
    }

    @Override
    public IRType solve() {
        node.solve();
        return get();
    }

}
