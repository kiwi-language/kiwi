package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import tech.metavm.autograph.mocks.ReachingDefFoo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReachingDefAnalyzerTest extends TestCase {

    public void test() {
        var psiFile = TranspileTestTools.getPsiJavaFile(ReachingDefFoo.class);
        AstToCfg astToCfg = new AstToCfg();
        psiFile.accept(astToCfg);
        psiFile.accept(new QnResolver());
        psiFile.accept(new ActivityAnalyzer());
        psiFile.accept(new ReachingDefAnalyzer(astToCfg.getGraphs()));
        psiFile.accept(new DefPrinter());
    }

    private static class DefPrinter extends JavaRecursiveElementVisitor {

        private final Set<PsiElement> visited = new HashSet<>();

        @Override
        public void visitElement(@NotNull PsiElement element) {
            if(visited.contains(element)) return;
            visited.add(element);
            var defs = element.getUserData(Keys.DEFINITIONS);
            if (defs != null) printDefs(element, defs);
            super.visitElement(element);
        }

        private void printDefs(PsiElement element, List<Definition> definitions) {
            System.out.println(element + ": " + definitions);
        }
    }


}