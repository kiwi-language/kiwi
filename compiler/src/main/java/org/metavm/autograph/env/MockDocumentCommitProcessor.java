package org.metavm.autograph.env;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DocumentCommitProcessor;
import com.intellij.psi.impl.PsiDocumentManagerBase;
import org.jetbrains.annotations.NotNull;

class MockDocumentCommitProcessor implements DocumentCommitProcessor {
    @Override
    public void commitSynchronously(@NotNull Document document, @NotNull Project project, @NotNull PsiFile psiFile) {
    }

    @Override
    public void commitAsynchronously(@NotNull Project project,
                                     @NotNull PsiDocumentManagerBase documentManager,
                                     @NotNull Document document,
                                     @NotNull Object reason,
                                     @NotNull ModalityState modality,
                                     @NotNull FileViewProvider cachedViewProvider) {

    }
}

