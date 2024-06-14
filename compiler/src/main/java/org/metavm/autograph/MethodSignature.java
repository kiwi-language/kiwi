package org.metavm.autograph;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;

import java.util.List;

public record MethodSignature(
        PsiClassType declaringClass,
        boolean isStatic,
        String name,
        List<PsiType> parameterClasses
) {

    public static MethodSignature create(PsiClassType declaringClass, String name, PsiType...parameterClasses) {
        return new MethodSignature(declaringClass, false, name, List.of(parameterClasses));
    }

    public static MethodSignature createStatic(PsiClassType declaringClass, String name, PsiType...parameterClasses) {
        return new MethodSignature(declaringClass, true, name, List.of(parameterClasses));
    }

    public boolean matches(MethodSignature that) {
        return declaringClass.isAssignableFrom(that.declaringClass)
                && isStatic == that.isStatic
                && name.equals(that.name) && parameterClasses.equals(that.parameterClasses);
    }

}
