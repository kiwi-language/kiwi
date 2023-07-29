package tech.metavm.autograph;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TranspileTestTools {

    private static final String BASE_MOD = "/Users/leen/Library/Java/JavaVirtualMachines/openjdk-18.0.2/Contents/Home/jmods/java.base.jmod";
    private static final String SOURCE_ROOT = "/Users/leen/workspace/object/src/test/java/";

    private static final IrCoreApplicationEnvironment APP_ENV;
    private static final IrCoreProjectEnvironment PROJECT_ENV;
    private static final Project PROJECT;

    static {
        APP_ENV = new IrCoreApplicationEnvironment(() -> {
        });
        PROJECT_ENV = new IrCoreProjectEnvironment(() -> {
        }, APP_ENV);
        var javaBaseDir = APP_ENV.getJarFileSystem().findFileByPath(BASE_MOD+"!/classes");
        PROJECT_ENV.addSourcesToClasspath(requireNonNull(javaBaseDir));
        PROJECT = PROJECT_ENV.getProject();
    }

    public static PsiJavaFile getPsiJavaFile(Class<?> klass) {
        return getPsiJavaFile(getClassFilePath(klass));
    }

    private static String getClassFilePath(Class<?> klass) {
        return SOURCE_ROOT + klass.getName().replace('.', '/') + ".java";
    }

    public static PsiJavaFile getPsiJavaFile(String path) {
        var file = APP_ENV.getLocalFileSystem().findFileByPath(path);
        assert file != null;
        return (PsiJavaFile) PsiManager.getInstance(PROJECT).findFile(file);
    }

}
