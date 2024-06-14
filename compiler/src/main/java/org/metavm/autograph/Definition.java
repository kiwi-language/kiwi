package org.metavm.autograph;

import com.intellij.psi.PsiElement;

public record Definition(PsiElement element, QualifiedName qqualifiedName) {
}
