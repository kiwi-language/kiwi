package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.TypeVariable;

public class XTypeImpl extends XType {

    private final TypeVariable<?> typeVariable;

    public XTypeImpl(TypeVariable<?> typeVariable) {
        super(typeVariable.getName());
        this.typeVariable = typeVariable;
    }

    public TypeVariable<?> getTypeVariable() {
        return typeVariable;
    }

}
