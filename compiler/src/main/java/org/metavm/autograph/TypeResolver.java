package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.object.type.*;

public interface TypeResolver {

    Type resolveTypeOnly(PsiType psiType);

    VariableType resolveTypeVariable(PsiTypeParameter typeParameter);

    Type resolveDeclaration(PsiType psiType);

    void addGeneratedKlass(Klass klass);

    Klass getKlass(PsiClass psiClass);

    void ensureDeclared(Klass classType);

    void ensureCodeGenerated(Klass classType);

    void mapCapturedType(PsiCapturedWildcardType psiCapturedWildcardType, CapturedType type);

    Type resolve(PsiType psiType);

    Type resolve(PsiType psiType, ResolutionStage stage);

    Field resolveField(PsiField field);

    Type resolveNullable(PsiType psiType, ResolutionStage stage);

    PsiCapturedWildcardType getPsiCapturedType(CapturedType capturedType);

    boolean isBuiltinClass(PsiClass klass);
}
