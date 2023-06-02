package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;

public record ExtensionArgumentNode(
        INode node,
        TypeVariable<IRClass> typeVariable,
        boolean isUpperBound
) implements VirtualNode {

    public static ExtensionArgumentNode upperBound(INode node, TypeVariable<IRClass> typeVariable) {
        return new ExtensionArgumentNode(node, typeVariable, true);
    }

    public static ExtensionArgumentNode lowerBound(INode node, TypeVariable<IRClass> typeVariable) {
        return new ExtensionArgumentNode(node, typeVariable, false);
    }

    @Override
    public IRType get() {
        var v = node.get();
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

    @Override
    public IRType solve() {
        node.solve();
        return get();
    }

}
