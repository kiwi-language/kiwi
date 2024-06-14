package org.metavm.autograph.env;

import com.intellij.formatting.service.FormattingService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.application.TransactionGuardImpl;
import com.intellij.openapi.editor.impl.DocumentWriteAccessGuard;
import com.intellij.psi.ClassTypePointerFactory;
import com.intellij.psi.JavaModuleSystem;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.JavaClassSupersImpl;
import com.intellij.psi.impl.RecordAugmentProvider;
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy;
import com.intellij.psi.impl.smartPointers.PsiClassReferenceTypePointerFactory;
import com.intellij.psi.impl.source.JShellPsiAugmentProvider;
import com.intellij.psi.impl.source.codeStyle.IndentHelper;
import com.intellij.psi.impl.source.tree.JavaTreeGenerator;
import com.intellij.psi.impl.source.tree.TreeCopyHandler;
import com.intellij.psi.impl.source.tree.TreeGenerator;
import com.intellij.psi.util.JavaClassSupers;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;
import org.metavm.util.ReflectionUtils;

public class IrCoreApplicationEnvironment extends JavaCoreApplicationEnvironment  {

    public IrCoreApplicationEnvironment(@NotNull Disposable parentDisposable) {
        super(parentDisposable);
        registerApplicationService(JavaClassSupers.class, new JavaClassSupersImpl());
        registerApplicationService(IndentHelper.class, new SimpleIndentHelper());
        registerApplicationService(TransactionGuard.class, createTransactionGuard()/*new TransactionGuardImpl()*/);

        //noinspection unchecked,rawtypes
        registerApplicationService(
                (Class) ReflectionUtils.classForName("com.intellij.diagnostic.PluginProblemReporter"),
                ReflectionUtils.newInstance(ReflectionUtils.classForName("com.intellij.diagnostic.PluginProblemReporterImpl"))
        );

        registerApplicationExtensionPoint(PsiAugmentProvider.EP_NAME, PsiAugmentProvider.class);
        registerApplicationExtensionPoint(JavaModuleSystem.EP_NAME, JavaModuleSystem.class);
        registerApplicationExtensionPoint(ClassTypePointerFactory.EP_NAME, ClassTypePointerFactory.class);
        registerApplicationExtensionPoint(TreeCopyHandler.EP_NAME, TreeCopyHandler.class);
        registerApplicationExtensionPoint(FormattingService.EP_NAME, FormattingService.class);
        registerApplicationExtensionPoint(DocumentWriteAccessGuard.EP_NAME, DocumentWriteAccessGuard.class);
        registerApplicationExtensionPoint(TreeGenerator.EP_NAME, TreeGenerator.class);
        registerApplicationExtensionPoint(ClsCustomNavigationPolicy.EP_NAME, ClsCustomNavigationPolicy.class);

        addExtension(PsiAugmentProvider.EP_NAME, new RecordAugmentProvider());
        addExtension(PsiAugmentProvider.EP_NAME, new JShellPsiAugmentProvider());
        addExtension(ClassTypePointerFactory.EP_NAME, new PsiClassReferenceTypePointerFactory());
        addExtension(TreeGenerator.EP_NAME, new JavaTreeGenerator());
//        addExtension(FormattingService.EP_NAME, new CoreFormattingService());
    }

    private TransactionGuard createTransactionGuard() {
        var theUnsafeField = ReflectionUtils.getField(Unsafe.class, "theUnsafe");
        theUnsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) ReflectionUtils.get(null, theUnsafeField);
        try {
            var guard =  (TransactionGuard) unsafe.allocateInstance(TransactionGuardImpl.class);
            var field1 = ReflectionUtils.getField(TransactionGuardImpl.class, "myWriteSafeModalities");
            field1.setAccessible(true);
            ReflectionUtils.set(guard, field1, CollectionFactory.createConcurrentWeakMap());
            var field2 = ReflectionUtils.getField(TransactionGuardImpl.class, "myWritingAllowed");
            field2.setAccessible(true);
            ReflectionUtils.set(guard, field2, false);
            return guard;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

}
