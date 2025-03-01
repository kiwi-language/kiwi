package org.metavm.compiler.element;

import org.metavm.compiler.util.List;

import javax.annotation.Nullable;

public interface GenericDeclaration {

    List<TypeVariable> getTypeParameters();

    void addTypeParameter(TypeVariable typeVariable);

    Object getInternalName(@Nullable Func current);

    ConstantPool getConstantPool();

}
