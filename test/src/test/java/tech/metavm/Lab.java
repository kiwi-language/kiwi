package tech.metavm;

import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.autograph.TranspileTestTools;
import tech.metavm.autograph.mocks.ReferenceFoo;
import tech.metavm.util.NncUtils;

import java.util.Objects;

public class Lab {

    public static final Logger logger = LoggerFactory.getLogger(Lab.class);


    public static void main(String[] args) {
        var file = TranspileTestTools.getPsiJavaFile(ReferenceFoo.class);
        var klass = file.getClasses()[0];
        var testMethod = klass.getMethods()[0];
        var fooMethod = klass.getMethods()[1];
        var methodCallExpr = (PsiMethodCallExpression) ((PsiExpressionStatement) Objects.requireNonNull(testMethod.getBody()).getStatements()[0]).getExpression();
        var methodRef = methodCallExpr.getMethodExpression();
        logger.info("method reference {}. java class: {}", methodRef.getText(), methodRef.getClass().getName());
        var resoled = (PsiMethod) methodRef.resolve();
        NncUtils.requireTrue(resoled == fooMethod);
    }

}
