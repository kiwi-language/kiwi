package org.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.metavm.autograph.env.IrCoreApplicationEnvironment;
import org.metavm.autograph.env.IrCoreProjectEnvironment;
import org.metavm.autograph.env.LightVirtualFileBase;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TranspileTestTools {

    public static final Logger LOGGER = LoggerFactory.getLogger(TranspileTestTools.class);

    private static final String BASE_MOD = "/Users/leen/Library/Java/JavaVirtualMachines/openjdk-18.0.2/Contents/Home/jmods/java.base.jmod";
    private static final String TEST_SOURCE_ROOT = "/Users/leen/workspace/object/test/src/test/java/";
    private static final String SOURCE_ROOT = "/Users/leen/workspace/object/compiler/src/main/java/";

    private static final IrCoreApplicationEnvironment APP_ENV;
    private static final IrCoreProjectEnvironment PROJECT_ENV;
    private static final Project PROJECT;
    private static final LightVirtualFileBase.MyVirtualFileSystem FILE_SYSTEM = LightVirtualFileBase.ourFileSystem;

    static {
        APP_ENV = new IrCoreApplicationEnvironment(() -> {
        });
        PROJECT_ENV = new IrCoreProjectEnvironment(() -> {
        }, APP_ENV);
        var javaBaseDir = APP_ENV.getJarFileSystem().findFileByPath(BASE_MOD + "!/classes");
        PROJECT_ENV.addSourcesToClasspath(requireNonNull(javaBaseDir));
        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(SOURCE_ROOT)));
        PROJECT_ENV.addSourcesToClasspath(requireNonNull(FILE_SYSTEM.findFileByPath(TEST_SOURCE_ROOT)));
        PROJECT = PROJECT_ENV.getProject();
        initTranspilerUtils();
    }

    public static void initTranspilerUtils() {
        TranspileUtils.init(PROJECT.getService(PsiElementFactory.class), PROJECT);
    }

    public static PsiJavaFile findFile(PsiDirectory directory, Class<?> klass) {
        return (PsiJavaFile) NncUtils.requireNonNull(directory.findFile(klass.getSimpleName() + ".java"));
    }

    public static PsiDirectory getDirectory(Package pkg) {
        return getDirectory(getPackagePath(pkg));
    }

    public static PsiDirectory getDirectory(String dir) {
        var file = NncUtils.requireNonNull(APP_ENV.getLocalFileSystem().findFileByPath(dir));
        return PsiManager.getInstance(PROJECT).findDirectory(file);
    }

    public static PsiJavaFile getPsiJavaFile(Class<?> klass) {
        return getPsiJavaFile(klass, false);
    }

    public static PsiClass getPsiClass(Class<?> klass) {
        var file = getPsiJavaFile(klass);
        return NncUtils.findRequired(
                file.getClasses(),
                k -> Objects.equals(k.getQualifiedName(), klass.getName())
        );
    }

    public static PsiClassType getPsiClassType(Class<?> klass) {
        return TranspileUtils.getElementFactory().createType(getPsiClass(klass));
    }

    public static PsiJavaFile getPsiJavaFile(Class<?> klass, boolean writable) {
        return getPsiJavaFile(getClassFilePath(klass), writable);
    }

    private static String getClassFilePath(Class<?> klass) {
        return TEST_SOURCE_ROOT + klass.getName().replace('.', '/') + ".java";
    }

    public static void executeCommand(Runnable command) {
        CommandProcessor.getInstance().executeCommand(
                getProject(),
                () -> {
                    try {
                        command.run();
                    } catch (Throwable e) {
                        LOGGER.error("fail to execute command", e);
                        if (e instanceof RuntimeException runtimeException) {
                            throw runtimeException;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                },
                null,
                null
        );
    }

    private static String getPackagePath(Package pkg) {
        return TEST_SOURCE_ROOT + pkg.getName().replace('.', '/');
    }

    public static PsiJavaFile getPsiJavaFile(String path) {
        return getPsiJavaFile(path, false);
    }

    public static PsiJavaFile getPsiJavaFile(String path, boolean writable) {
        var file = NncUtils.requireNonNull(FILE_SYSTEM.findFileByPath(path));
        try {
            if (writable) file.setWritable(true);
            return (PsiJavaFile) PsiManager.getInstance(PROJECT).findFile(file);
        } catch (IOException e) {
            throw new InternalException("Fail to set writable of file " + path, e);
        }
    }

    public static Project getProject() {
        return PROJECT;
    }

    public static void touch() {
        initTranspilerUtils();
    }

}
