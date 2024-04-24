package tech.metavm.autograph;

import com.intellij.psi.*;
import tech.metavm.object.type.*;

import java.util.Set;

public interface TypeResolver {

    Type resolveTypeOnly(PsiType psiType);

    VariableType resolveTypeVariable(PsiTypeParameter typeParameter);

    Type resolveDeclaration(PsiType psiType);

    void ensureDeclared(Klass classType);

    void ensureCodeGenerated(Klass classType);

    void mapCapturedType(PsiCapturedWildcardType psiCapturedWildcardType, CapturedType type);

    Type resolve(PsiType psiType);

    Field resolveField(PsiField field);

    PsiCapturedWildcardType getPsiCapturedType(CapturedType capturedType);

}
