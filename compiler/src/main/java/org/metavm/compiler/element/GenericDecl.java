package org.metavm.compiler.element;

import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;

public interface GenericDecl {

    List<TypeVar> getTypeParams();

    void addTypeParam(TypeVar typeVar);

    Object getInternalName(@Nullable Func current);

//    ConstPool getConstPool();

    Element getInst(List<Type> typeArgs);

}
