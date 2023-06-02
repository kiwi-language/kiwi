package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;

import java.util.Map;

public interface TypeGraph {

    INode getNode(IRType type);

    default boolean addExtension(IRType type1, IRType type2) {
        return addRelation(TypeRelation.extends_(type1, type2));
    }

    default boolean addSuper(IRType type1, IRType type2) {
        return addRelation(TypeRelation.super_(type1, type2));
    }

    boolean addRelation(TypeRelation relation);

    Map<XType, IRType> getSolution();

    boolean isSolvable();

}
