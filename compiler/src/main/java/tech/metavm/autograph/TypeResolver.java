package tech.metavm.autograph;

import com.intellij.psi.PsiCapturedWildcardType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import tech.metavm.object.type.*;

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
