package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.PsiType;
import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.autograph.mocks.PTypeFoo;
import tech.metavm.autograph.mocks.TypeFoo;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class TranspileUtilTest extends TestCase {

    public void testGetTemplateType() {
        Visitor visitor = new Visitor();
        var foo = TranspileTestTools.getPsiJavaFile(PTypeFoo.class);
        foo.accept(visitor);
    }

    public void testMatchMethod() {
        var method = ReflectUtils.getMethod(List.class, "get", int.class);
        var psiClass = TranspileTestTools.getPsiClass(TypeFoo.class);
        var psiMethod = NncUtils.findRequired(psiClass.getMethods(), m -> m.getName().equals("get"));
        NncUtils.requireTrue(TranspileUtil.matchMethod(psiMethod, method));
    }

    private static class Visitor extends JavaRecursiveElementVisitor {

        private PsiType templateType;

        @Override
        public void visitThisExpression(PsiThisExpression expression) {
            PsiType selfType = expression.getType();
            Assert.assertEquals(selfType, templateType);
        }

        @Override
        public void visitField(PsiField field) {
            super.visitField(field);
            templateType = TranspileUtil.createTemplateType(requireNonNull(field.getContainingClass()));
            super.visitField(field);
        }
    }

}