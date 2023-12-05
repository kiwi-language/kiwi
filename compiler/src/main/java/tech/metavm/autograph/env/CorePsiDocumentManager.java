package tech.metavm.autograph.env;

import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.PsiDocumentManagerBase;
import org.jetbrains.annotations.NotNull;

final class CorePsiDocumentManager extends PsiDocumentManagerBase {
    CorePsiDocumentManager(@NotNull Project project) {
        super(project);
    }
}

