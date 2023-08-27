package tech.metavm.autograph;

import com.intellij.psi.PsiType;
import tech.metavm.entity.DefContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;

public class DefContextTypeResolver implements TypeResolver {

    private final DefContext defContext;

    public DefContextTypeResolver(DefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public Type resolve(PsiType psiType) {



        return null;
    }

    @Override
    public void addType(String name, ClassType classType) {

    }
}
