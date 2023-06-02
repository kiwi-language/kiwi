package tech.metavm.transpile.ir;


public interface InternalGenericDeclaration<D extends GenericDeclaration<D>> extends GenericDeclaration<D> {

        void addTypeParameter(TypeVariable<D> typeVariable);

}
