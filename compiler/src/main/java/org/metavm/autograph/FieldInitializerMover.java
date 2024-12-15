package org.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FieldInitializerMover extends VisitorBase {

    @Override
    public void visitField(PsiField field) {
        super.visitField(field);
        var initializer = field.getInitializer();
        var klass = requireNonNull(field.getContainingClass());
        var methodName = "__init_" + field.getName() + "__";
        var method = NncUtils.find(klass.getMethods(),
                m -> m.getName().equals(methodName) && m.getParameterList().isEmpty());
        if(method == null && initializer != null) {
            var isStatic = TranspileUtils.isStatic(field);
            method = (PsiMethod) klass.addBefore(TranspileUtils.createMethodFromText(
                    "private "+ (isStatic ? "static " : " ")
                            + field.getType().getCanonicalText() + " " + methodName + "() {}"), null
            );
            var body = requireNonNull(method.getBody());
            body.addAfter(
                    TranspileUtils.createStatementFromText("return " + initializer.getText() + ";"),
                    null
            );
        }
        if(method != null)
            field.putUserData(Keys.INITIALIZER, method);
        if(initializer != null)
            field.setInitializer(null);
    }

}
