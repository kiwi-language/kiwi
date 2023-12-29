package tech.metavm.autograph;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;

import java.util.List;

public record MethodSignature(
        PsiClassType declaringClass,
        String name,
        List<PsiType> parameterClasses
) {

    public static MethodSignature create(PsiClassType declaringClass, String name, PsiType...parameterClasses) {
        return new MethodSignature(declaringClass, name, List.of(parameterClasses));
    }

    public boolean matches(MethodSignature that) {
        return declaringClass.isAssignableFrom(that.declaringClass)
                && name.equals(that.name) && parameterClasses.equals(that.parameterClasses);
    }

}
