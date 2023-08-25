package tech.metavm.autograph;

import com.intellij.psi.PsiType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;

public interface TypeResolver {

    Type resolve(PsiType psiType);

    void addType(String name, ClassType classType);

}
