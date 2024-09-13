package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.object.type.*;

public interface TypeResolver {

    Type resolveTypeOnly(PsiType psiType);

    VariableType resolveTypeVariable(PsiTypeParameter typeParameter);

    Type resolveDeclaration(PsiType psiType);

    void addGeneratedTypeDef(TypeDef typeDef);

    Klass getKlass(PsiClass psiClass);

    void ensureDeclared(Klass classType);

    void ensureCodeGenerated(Klass classType);

    void mapCapturedType(PsiCapturedWildcardType psiCapturedWildcardType, CapturedType type);

    Type resolve(PsiType psiType);

    Field resolveField(PsiField field);

    PsiCapturedWildcardType getPsiCapturedType(CapturedType capturedType);

}
