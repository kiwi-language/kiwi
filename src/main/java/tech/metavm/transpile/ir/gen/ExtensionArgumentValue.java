package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;

public record ExtensionArgumentValue(
        Value value,
        TypeVariable<IRClass> typeVariable,
        boolean isUpperBound
) implements Value {

    public static ExtensionArgumentValue upperBound(Value value, TypeVariable<IRClass> typeVariable) {
        return new ExtensionArgumentValue(value, typeVariable, true);
    }

    public static ExtensionArgumentValue lowerBound(Value value, TypeVariable<IRClass> typeVariable) {
        return new ExtensionArgumentValue(value, typeVariable, false);
    }

    @Override
    public IRType get() {
        var v = value.get();
        if(v instanceof IRAnyType) {
            return IRAnyType.getInstance();
        }
        else if(v instanceof PType pType) {
            var ancestor = (PType) IRUtil.getAncestorType(pType, typeVariable.getGenericDeclaration());
            var arg = ancestor.getTypeArgument(
                    typeVariable.getGenericDeclaration().typeParameters().indexOf(typeVariable)
            );
            return isUpperBound ? arg.getUpperBound() : arg.getLowerBound();
        }
        else {
            throw new InternalException("Invalid pNode value " + v);
        }
    }

}
