package org.metavm.autograph;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.metavm.autograph.env.IrCoreApplicationEnvironment;
import org.metavm.autograph.env.IrCoreProjectEnvironment;

import static java.util.Objects.requireNonNull;

public class PsiLab {

    public static final String JDK_HOME = "/Users/leen/Library/Java/JavaVirtualMachines/openjdk-18.0.2/Contents/Home";
    public static final String FOO_FILE = "/Users/leen/workspace/psilab/src/main/java/org.metavm/psi/PsiFoo.java";
    public static final String BAR_FILE = "/Users/leen/workspace/psilab/src/main/java/org.metavm/psi/PsiBar.java";
    public static final String ROOT = "/Users/leen/workspace/psilab/src/main/java";

    private static final IrCoreApplicationEnvironment APP_ENV = new IrCoreApplicationEnvironment(() -> {});
    private static final IrCoreProjectEnvironment PROJECT_ENV = new IrCoreProjectEnvironment(() -> {}, APP_ENV);

    public static void main(String[] args) {
        PROJECT_ENV.addSourcesToClasspath(
                requireNonNull(
                        APP_ENV.getJarFileSystem().findFileByPath(JDK_HOME + "/jmods/java.base.jmod!/classes")
                )
        );
        PROJECT_ENV.addSourcesToClasspath(
                requireNonNull(
                        APP_ENV.getLocalFileSystem().findFileByPath("/Users/leen/workspace/psilab/src/main/java")
                )
        );

        var psiManager = PsiManager.getInstance(PROJECT_ENV.getProject());
        var dir = psiManager.findDirectory(
                requireNonNull(APP_ENV.getLocalFileSystem().findFileByPath(ROOT))
        );

        psiManager.getProject();
        var foo = readClass(FOO_FILE);
        var fooFile = foo.getContainingFile();

        var method = foo.getMethods()[0];
        var stmt = requireNonNull(method.getBody()).getStatements()[0];
        var lambda =
                (PsiLambdaExpression) ((PsiMethodCallExpression) ((PsiExpressionStatement) stmt).getExpression()).getArgumentList().getExpressions()[0];
        var funcType = lambda.getFunctionalInterfaceType();
        var refBar = ((PsiClassReferenceType) foo.getFields()[0].getType()).resolve();
        var bar = readClass(BAR_FILE);

        System.out.println("Same bar: " + (refBar == bar));
        System.out.println(funcType);
        var methods = foo.getMethods();
        System.out.println(methods[0]);
    }

    private static PsiClass readClass(String path) {
        var vf = requireNonNull(APP_ENV.getLocalFileSystem().findFileByPath(path));
        var psiFile = requireNonNull((PsiJavaFile) PsiManager.getInstance(PROJECT_ENV.getProject()).findFile(vf));
        return psiFile.getClasses()[0];
    }

}
