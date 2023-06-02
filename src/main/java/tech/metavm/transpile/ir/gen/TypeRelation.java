package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.util.InternalException;

public record TypeRelation(
        IRType first,
        RelationKind kind,
        IRType second
) {

    IRType theOther(IRType type) {
        if(first == type) {
            return second;
        }
        if(second == type) {
            return first;
        }
        throw new InternalException(type + " is not an operand of " + this);
    }

//    static TypeRelation eq(IRType first, IRType second) {
//        return new TypeRelation(first, RelationKind.EQ, second);
//    }

    static TypeRelation extends_(IRType first, IRType second) {
        return new TypeRelation(first, RelationKind.EXTENDS, second);
    }

    static TypeRelation super_(IRType first, IRType second) {
        return new TypeRelation(first, RelationKind.SUPER, second);
    }

}
