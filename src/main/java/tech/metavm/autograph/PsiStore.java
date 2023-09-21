package tech.metavm.autograph;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import tech.metavm.autograph.env.IrCoreApplicationEnvironment;
import tech.metavm.autograph.env.IrCoreProjectEnvironment;

import static java.util.Objects.requireNonNull;

public class PsiStore {

    public static final String JDK_HOME = "/Users/leen/Library/Java/JavaVirtualMachines/openjdk-18.0.2/Contents/Home";
    public static final String CP_JAVA_BASE = JDK_HOME + "/jmods/java.base.jmod!/classes";
    public static final String PROJECT_ROOT = "/Users/leen/workspace";
    public static final String SOURCE_ROOT = PROJECT_ROOT + "/src/main/java";
    public static PsiStore INSTANCE = new PsiStore();

    private final IrCoreApplicationEnvironment appEnv = new IrCoreApplicationEnvironment(() -> {});
    private final Project project;

    public PsiStore() {
        IrCoreProjectEnvironment projectENv = new IrCoreProjectEnvironment(() -> {}, appEnv);
        projectENv.addSourcesToClasspath(
                requireNonNull(appEnv.getJarFileSystem().findFileByPath(CP_JAVA_BASE))
        );
        projectENv.addSourcesToClasspath(
                requireNonNull(appEnv.getLocalFileSystem().findFileByPath(SOURCE_ROOT))
        );
        project = projectENv.getProject();
    }

    public PsiJavaFile getPsiFile(Class<?> klass) {
        return getPsiFile(klass.getName());
    }

    public PsiJavaFile getPsiFile(String className) {
        var vf = requireNonNull(appEnv.getLocalFileSystem().findFileByPath(getClassFilePath(className)));
        return (PsiJavaFile) PsiManager.getInstance(project).findFile(vf);
    }

    public PsiDirectory getPsiDirectory(String dir) {
        var vf = requireNonNull(appEnv.getLocalFileSystem().findFileByPath(dir));
        return PsiManager.getInstance(project).findDirectory(vf);
    }

    private String getClassFilePath(String className) {
        return SOURCE_ROOT + "/" + className.replace('.', '/') + ".java";
    }


}
