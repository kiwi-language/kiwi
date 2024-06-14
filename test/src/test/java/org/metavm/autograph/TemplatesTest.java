package org.metavm.autograph;

import com.intellij.psi.PsiStatement;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Map;
import java.util.Objects;

public class TemplatesTest extends TestCase {

    private Templates templates;

    @Override
    protected void setUp() throws Exception {
        templates = new Templates(TranspileTestTools.getProject());
    }

    public void testExpression() {
        var l1 = templates.replaceAsExpression("1L", Map.of());
        var l2 = templates.replaceAsExpression("2L", Map.of());

        var add = templates.replaceAsExpression("a + b", Map.of("a", l1, "b", l2));
        Assert.assertNotNull(add);
        System.out.println(add);
    }

    public void testStatements() {
        var stmts = templates.replaceAsStatements("a=1", Map.of());
        Assert.assertNotNull(stmts);
        System.out.println(stmts);
    }

    public void testReplaceAsMethod() {
        String template = """
                public void foo(int v) {
                    return v + 1;
                }
                """;
        var method = templates.replaceAsMethod(template, Map.of());
        Assert.assertNotNull(method);
        System.out.println(method);
        var stmts = Objects.requireNonNull(method.getBody()).getStatements();
        for (PsiStatement stmt : stmts) {
            System.out.println(stmt);
        }
    }


}