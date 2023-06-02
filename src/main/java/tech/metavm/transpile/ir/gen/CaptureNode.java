package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.CaptureType;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.IRWildCardType;

public class CaptureNode implements VirtualNode {

    private final INode range;

    protected CaptureNode(INode range) {
        this.range = range;
    }

    @Override
    public IRType get() {
        return new CaptureType((IRWildCardType) range.getType());
    }

    @Override
    public IRType solve() {
        range.solve();
        return get();
    }

}
