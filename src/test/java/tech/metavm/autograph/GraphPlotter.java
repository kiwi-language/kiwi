package tech.metavm.autograph;

import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.util.Objects;

public class GraphPlotter  {

    public static final String PATH =
//            "/Users/leen/workspace/object/src/main/java/tech/metavm/autograph/GraphViewer.java";
            "/Users/leen/workspace/object/src/test/java/tech/metavm/autograph/mocks/AutographFoo.java";

    public static void main(String[] args) {
        var appEnv = new IrCoreApplicationEnvironment(() -> {});
        var projectEnv = new IrCoreProjectEnvironment(() -> {}, appEnv);
        var project = projectEnv.getProject();
        var file = appEnv.getLocalFileSystem().findFileByPath(PATH);
        assert file != null;
        var psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
        var visitor = new AstToCfg();
        Objects.requireNonNull(psiJavaFile).accept(visitor);
        var graphs = visitor.getGraphs().values();
        for (Graph graph : graphs) {
            new GraphViewer().display(graph);
        }
    }

}