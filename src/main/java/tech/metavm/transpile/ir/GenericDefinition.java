package tech.metavm.transpile.ir;

public interface GenericDefinition {

    IRType resolve(TypeVariable<?> typeVariable);

}
