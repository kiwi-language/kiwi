package tech.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

public interface TypeResolver {

    Type resolveTypeOnly(PsiType psiType, IEntityContext context);

    Type resolveDeclaration(PsiType psiType, IEntityContext context);

    Type resolve(PsiType psiType, IEntityContext context);

    Field resolveField(PsiField field, IEntityContext context);

}
