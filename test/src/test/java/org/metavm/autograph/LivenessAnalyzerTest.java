package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiForeachStatement;
import junit.framework.TestCase;

import java.util.Objects;

public class LivenessAnalyzerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.LivenessFoo");
        file.accept(new QnResolver());
        file.accept(new ActivityAnalyzer());
        file.accept(new ActivityPrinter());
        AstToCfg astToCfg = new AstToCfg();
        file.accept(astToCfg);
//        GraphPlotter.plot(astToCfg.getGraphs().values().iterator().next());
        var analyzer = new LivenessAnalyzer(astToCfg.getGraphs());
        file.accept(analyzer);
        file.accept(new LivenessPrinter());
    }

    private static class ActivityPrinter extends JavaRecursiveElementVisitor {

        @Override
        public void visitForeachStatement(PsiForeachStatement statement) {
            var scope = Objects.requireNonNull(statement.getUserData(Keys.BODY_SCOPE));
            System.out.println("read: " + scope.getRead());
            System.out.println("modified: " + scope.getModified());
        }
    }

    private static class LivenessPrinter extends JavaRecursiveElementVisitor {

        @Override
        public void visitForeachStatement(PsiForeachStatement statement) {
            var liveIn = statement.getUserData(Keys.LIVE_VARS_IN);
            System.out.println("live out: " + liveIn);
            System.out.println("live in: " + liveIn);
        }
    }

}