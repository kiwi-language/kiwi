package org.metavm.asm;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.GenericDeclaration;
import org.metavm.flow.Method;
import org.metavm.object.type.TypeVariable;

import java.util.List;
import java.util.Objects;

final class AsmMethod extends AsmCallable implements AsmGenericDeclaration {

    AsmMethod(AsmKlass parent, Method method) {
        super(parent, method);
        if(!method.isStatic())
            nextVariableIndex();
    }

    @Override
    @NotNull
    public AsmKlass parent() {
        return (AsmKlass) Objects.requireNonNull(super.parent());
    }

    @Override
    public Method getCallable() {
        return (Method) super.getCallable();
    }

    @Override
    public List<TypeVariable> getTypeParameters() {
        return getCallable().getTypeParameters();
    }

    @Override
    public GenericDeclaration getGenericDeclaration() {
        return getCallable();
    }

    @Override
    public String toString() {
        return "AsmMethod " + getCallable().getQualifiedSignature();
    }

}
