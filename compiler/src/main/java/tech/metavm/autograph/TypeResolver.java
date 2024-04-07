package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.object.type.*;

import java.util.Set;

public interface TypeResolver {

    Type resolveTypeOnly(PsiType psiType);

    TypeVariable resolveTypeVariable(PsiTypeParameter typeParameter);

    Type resolveDeclaration(PsiType psiType);

    void ensureDeclared(ClassType classType);

    void ensureCodeGenerated(ClassType classType);

    void mapCapturedType(PsiCapturedWildcardType psiCapturedWildcardType, CapturedType type);

    Type resolve(PsiType psiType);

    Field resolveField(PsiField field);

    Set<Type> getGeneratedTypes();

    PsiCapturedWildcardType getPsiCapturedType(CapturedType capturedType);

}
