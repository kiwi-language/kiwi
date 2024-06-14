package org.metavm.util;

import com.intellij.psi.PsiMethod;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class CompilerConfig {

    private static volatile Set<String> methodBlacklist = Set.of();

    public static void setMethodBlacklist(Set<String> methodBlacklist) {
        CompilerConfig.methodBlacklist = methodBlacklist;
    }

    public static boolean isMethodBlacklisted(PsiMethod method) {
        var methodName = requireNonNull(method.getContainingClass()).getQualifiedName() + "." + method.getName();
        return methodBlacklist.contains(methodName);
    }

}
