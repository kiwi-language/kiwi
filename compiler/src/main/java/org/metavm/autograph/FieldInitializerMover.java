package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
public class FieldInitializerMover extends VisitorBase {

    private final Set<PsiStatement> movedInitializers = new HashSet<>();

    @Override
    public void visitClass(PsiClass aClass) {
        if(!(aClass instanceof PsiTypeParameter)) {
            if (!aClass.isInterface()) {
                if(NncUtils.find(aClass.getMethods(), m -> m.getName().equals("__init__")) == null)
                    aClass.addBefore(TranspileUtils.createMethodFromText("private void __init__() {}"), null);
                var initCall = TranspileUtils.createStatementFromText("__init__();");
                for (PsiMethod method : aClass.getMethods()) {
                    if (method.isConstructor()) {
                        var block = requireNonNull(method.getBody());
                        if (block.getStatements().length == 0)
                            block.add(initCall);
                        else {
                            var firstStmt = block.getStatements()[0];
                            if (!TranspileUtils.isThisCall(firstStmt)) {
                                if (TranspileUtils.isSuperCall(firstStmt))
                                    block.addAfter(initCall, firstStmt);
                                else
                                    block.addAfter(initCall, null);
                            }
                        }
                    }
                }
            }
            if(NncUtils.find(aClass.getMethods(), m -> m.getName().equals("__cinit__")) == null)
                aClass.addBefore(TranspileUtils.createMethodFromText("private static void __cinit__() {}"), null);
        }
        super.visitClass(aClass);
    }

    @Override
    public void visitField(PsiField field) {
        super.visitField(field);
        var initializer = field.getInitializer();
        if(initializer != null) {
            var klass = requireNonNull(field.getContainingClass());
            var isStatic = TranspileUtils.isStatic(field);
            var method =  isStatic ?
                    TranspileUtils.getMethodByName(klass, "__cinit__") :
                    TranspileUtils.getMethodByName(klass, "__init__");
//            initializer.accept(new InitializerTransformer());
            var body = requireNonNull(method.getBody());
            var statements = body.getStatements();
            PsiStatement anchor;
            if (statements.length > 0) {
                int i = TranspileUtils.isSuperCall(statements[0]) ? 1 : 0;
                //noinspection StatementWithEmptyBody
                for (; i < statements.length && movedInitializers.contains(statements[i]); i++) ;
                anchor = i > 0 ? statements[i - 1] : null;
            } else
                anchor = null;
            movedInitializers.add(
                    (PsiStatement) body.addAfter(
                            TranspileUtils.createStatementFromText(
                                    (isStatic ? klass.getName() : "this") + "."
                                            + field.getName() + "=" + initializer.getText() + ";"
                            ),
                            anchor
                    )
            );
            field.setInitializer(null);
        }
    }

}
