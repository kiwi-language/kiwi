package org.metavm.autograph;

import com.intellij.psi.PsiReturnStatement;
import junit.framework.TestCase;
import org.metavm.autograph.mocks.StringConcatFoo;

import java.util.Objects;

public class StringConcatTransformerTest extends TestCase {

    public void test() {
        var file = TranspileTestTools.getPsiJavaFile(StringConcatFoo.class);
        TranspileTestTools.executeCommand(() -> file.accept(new StringConcatTransformer()));
        var stmt = (PsiReturnStatement) Objects.requireNonNull(file.getClasses()[0].getMethods()[0].getBody())
                .getStatements()[0];
        var expr = Objects.requireNonNull(stmt.getReturnValue());
        System.out.println(expr.getText());
    }

}