package tech.metavm.transpile.ir;

public final class MethodBlock extends CodeBlock {
    private final IRMethod method;

    public MethodBlock(IBlock parent, IRMethod method) {
        super(parent);
        this.method = method;
    }

    public IRMethod method() {
        return method;
    }

}
