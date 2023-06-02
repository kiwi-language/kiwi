package tech.metavm.transpile.ir;

public interface IRExpression extends Statement {

    IRType type();

    default IRClass klass() {
        return IRUtil.getRawClass(type());
    }

}
