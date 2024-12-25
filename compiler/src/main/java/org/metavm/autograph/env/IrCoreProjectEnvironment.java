package org.metavm.autograph.env;

import com.intellij.lang.jvm.facade.JvmElementProvider;
import com.intellij.openapi.Disposable;
import com.intellij.pom.PomModel;
import com.intellij.pom.core.impl.PomModelImpl;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.PsiTreeChangeListener;
//import com.intellij.psi.codeStyle.CodeStyleManager;
//import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
//import com.intellij.psi.codeStyle.ProjectCodeStyleSettingsManager;
import com.intellij.psi.impl.PsiElementFinderImpl;
import com.intellij.psi.impl.PsiNameHelperImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessor;
//import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import org.jetbrains.annotations.NotNull;

import static com.intellij.core.CoreApplicationEnvironment.registerExtensionPoint;

public class IrCoreProjectEnvironment extends JavaCoreProjectEnvironment {
    public IrCoreProjectEnvironment(@NotNull Disposable parentDisposable, @NotNull IrCoreApplicationEnvironment applicationEnvironment) {
        super(parentDisposable, applicationEnvironment);
        //noinspection UnstableApiUsage
        var projectExtArea = getProject().getExtensionArea();
        getProject().registerService(TreeAspect.class, TreeAspect.class);
        getProject().registerService(PomModel.class, PomModelImpl.class);
//        getProject().registerService(CodeStyleManager.class, CodeStyleManagerImpl.class);
//        getProject().registerService(CodeStyleSettingsManager.class, ProjectCodeStyleSettingsManager.class);
//        getProject().registerService(ProjectCodeStyleSettingsManager.class, ProjectCodeStyleSettingsManager.class);
        getProject().registerService(PsiNameHelper.class, PsiNameHelperImpl.class);

        registerExtensionPoint(projectExtArea, "com.intellij.java.elementFinder", PsiElementFinder.class);
        registerExtensionPoint(projectExtArea, JvmElementProvider.EP_NAME, JvmElementProvider.class);
        registerExtensionPoint(projectExtArea, PsiTreeChangePreprocessor.EP.getName(), PsiTreeChangePreprocessor.class);
        registerExtensionPoint(projectExtArea, PsiTreeChangeListener.EP.getName(), PsiTreeChangeListener.class);

        //noinspection UnstableApiUsage
        projectExtArea.getExtensionPoint("com.intellij.java.elementFinder")
                .registerExtension(
                        new PsiElementFinderImpl(getProject()), applicationEnvironment.getParentDisposable()
                );
    }
}
