package org.metavm.asm;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.GenericDeclaration;
import org.metavm.flow.Lambda;
import org.metavm.object.type.TypeVariable;

import java.util.List;

public class AsmLambda extends AsmCallable {

    AsmLambda(AsmCallable parent, Lambda callable) {
        super(parent, callable);
    }

    @Override
    public Lambda getCallable() {
        return (Lambda) super.getCallable();
    }

    @Override
    public @NotNull AsmCallable parent() {
        return (AsmCallable) super.parent();
    }

    @Override
    public List<TypeVariable> getTypeParameters() {
        return List.of();
    }

    @Override
    public GenericDeclaration getGenericDeclaration() {
        return null;
    }

    @Override
    public String toString() {
        return "AsmLambda";
    }
}
