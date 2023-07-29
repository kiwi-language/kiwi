package tech.metavm.autograph;

import com.intellij.core.CoreApplicationEnvironment;
import com.intellij.core.JavaCoreProjectEnvironment;
import com.intellij.lang.jvm.facade.JvmElementProvider;
import com.intellij.openapi.Disposable;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.impl.PsiElementFinderImpl;
import org.jetbrains.annotations.NotNull;

import static com.intellij.core.CoreApplicationEnvironment.registerExtensionPoint;

public class IrCoreProjectEnvironment extends JavaCoreProjectEnvironment {
    public IrCoreProjectEnvironment(@NotNull Disposable parentDisposable, @NotNull CoreApplicationEnvironment applicationEnvironment) {
        super(parentDisposable, applicationEnvironment);
        //noinspection UnstableApiUsage
        var projectExtArea = getProject().getExtensionArea();
        registerExtensionPoint(projectExtArea, "com.intellij.java.elementFinder", PsiElementFinder.class);
        registerExtensionPoint(projectExtArea, JvmElementProvider.EP_NAME, JvmElementProvider.class);
        //noinspection UnstableApiUsage
        projectExtArea.getExtensionPoint("com.intellij.java.elementFinder")
                .registerExtension(
                        new PsiElementFinderImpl(getProject()), applicationEnvironment.getParentDisposable()
                );
    }
}
