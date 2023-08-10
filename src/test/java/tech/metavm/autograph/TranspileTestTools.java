package tech.metavm.autograph;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import tech.metavm.util.InternalException;

import java.io.File;
import java.io.IOException;
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
        return getPsiJavaFile(klass, false);
    }

    public static PsiJavaFile getPsiJavaFile(Class<?> klass, boolean writable) {
        return getPsiJavaFile(getClassFilePath(klass), writable);
    }

    private static String getClassFilePath(Class<?> klass) {
        return SOURCE_ROOT + klass.getName().replace('.', '/') + ".java";
    }

    public static PsiJavaFile getPsiJavaFile(String path) {
        return getPsiJavaFile(path, false);
    }

    public static PsiJavaFile getPsiJavaFile(String path, boolean writable) {
        var file = APP_ENV.getLocalFileSystem().findFileByIoFile(new File(path));
        assert file != null;
        try {
            if(writable) file.setWritable(true);
            return (PsiJavaFile) PsiManager.getInstance(PROJECT).findFile(file);
        } catch (IOException e) {
            throw new InternalException("Fail to set writable of file " + path, e);
        }
    }

    public static Project getProject() {
        return PROJECT;
    }
}
