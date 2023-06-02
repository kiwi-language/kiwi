package tech.metavm.transpile.ir;

public interface UnresolvedExpression extends IRExpression {

    boolean isTypeMatched(IRType type);

    IRExpression resolve(IRType type);

    @Override
    default IRType type() {
        throw new UnsupportedOperationException();
    }

    default IRType onResolved(IRType type) {
        return type;
    }

}
