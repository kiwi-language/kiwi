package tech.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeVariable;

import java.util.Set;

public interface TypeResolver {

    Type resolveTypeOnly(PsiType psiType);

    TypeVariable resolveTypeVariable(PsiTypeParameter typeParameter);

    Type resolveDeclaration(PsiType psiType);

    void ensureDeclared(ClassType classType);

    void ensureCodeGenerated(ClassType classType);

    Type resolve(PsiType psiType);

    Field resolveField(PsiField field);

    Set<Type> getGeneratedTypes();

}
