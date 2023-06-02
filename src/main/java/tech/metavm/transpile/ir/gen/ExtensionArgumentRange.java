package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;

public record ExtensionArgumentRange(
        Value value,
        TypeVariable<IRClass> typeVariable
) implements Range {

    @Override
    public Value getLowerBound() {
        return get(false);
    }

    @Override
    public Value getUpperBound() {
        return get(true);
    }

    private Value get(boolean forMax) {
        var v = value.get();
        if(v instanceof IRAnyType) {
            return IRAnyType::getInstance;
        }
        else if(v instanceof PType pType) {
            var ancestor = (PType) IRUtil.getAncestorType(pType, typeVariable.getGenericDeclaration());
            var arg = ancestor.getTypeArgument(
                    typeVariable.getGenericDeclaration().typeParameters().indexOf(typeVariable)
            );
            return forMax ? arg::getUpperBound : arg::getLowerBound;
        }
        else {
            throw new InternalException("Invalid pNode value " + v);
        }
    }

}
