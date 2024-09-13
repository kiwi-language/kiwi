package org.metavm.autograph;

import com.intellij.psi.PsiJavaFile;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record CompileStage(
        Predicate<PsiJavaFile> filter,
        Consumer<PsiJavaFile> action) {

}
