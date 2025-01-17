package org.metavm.asm;

import org.metavm.entity.GenericDeclaration;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;

public interface AsmScope {

    AsmCompilationUnit getCompilationUnit();

    @Nullable
    AsmScope parent();

    List<TypeVariable> getTypeParameters();

    GenericDeclaration getGenericDeclaration();

    default @Nullable TypeVariable findTypeParameter(String name) {
        return Utils.find(getTypeParameters(), tv -> tv.getName().equals(name));
    }

}
