package tech.metavm.autograph;

import com.intellij.core.JavaCoreApplicationEnvironment;
import com.intellij.openapi.Disposable;
import com.intellij.psi.ClassTypePointerFactory;
import com.intellij.psi.JavaModuleSystem;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.JavaClassSupersImpl;
import com.intellij.psi.impl.RecordAugmentProvider;
import com.intellij.psi.impl.smartPointers.PsiClassReferenceTypePointerFactory;
import com.intellij.psi.impl.source.JShellPsiAugmentProvider;
import com.intellij.psi.impl.source.codeStyle.IndentHelper;
import com.intellij.psi.impl.source.codeStyle.IndentHelperImpl;
import com.intellij.psi.impl.source.tree.TreeCopyHandler;
import com.intellij.psi.util.JavaClassSupers;
import org.jetbrains.annotations.NotNull;

public class IrCoreApplicationEnvironment extends JavaCoreApplicationEnvironment  {

    public IrCoreApplicationEnvironment(@NotNull Disposable parentDisposable) {
        super(parentDisposable);
        registerApplicationService(JavaClassSupers.class, new JavaClassSupersImpl());
        registerApplicationService(IndentHelper.class, new IndentHelperImpl());
        registerApplicationExtensionPoint(PsiAugmentProvider.EP_NAME, PsiAugmentProvider.class);
        registerApplicationExtensionPoint(JavaModuleSystem.EP_NAME, JavaModuleSystem.class);
        registerApplicationExtensionPoint(ClassTypePointerFactory.EP_NAME, ClassTypePointerFactory.class);
        registerApplicationExtensionPoint(TreeCopyHandler.EP_NAME, TreeCopyHandler.class);
        addExtension(PsiAugmentProvider.EP_NAME, new RecordAugmentProvider());
        addExtension(PsiAugmentProvider.EP_NAME, new JShellPsiAugmentProvider());
        addExtension(ClassTypePointerFactory.EP_NAME, new PsiClassReferenceTypePointerFactory());
    }
}
