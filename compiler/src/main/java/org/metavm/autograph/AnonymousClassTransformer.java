package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class AnonymousClassTransformer extends VisitorBase {

    private final Map<PsiClass, String> map = new HashMap<>();
    private final LinkedList<ExecutableInfo> executables = new LinkedList<>();
    private int nextId = 1;

    @Override
    public void visitJavaFile(PsiJavaFile file) {
        var maxCount = file.getUserData(Keys.MAX_SYNTHETIC_CLASS_SEQ);
        if(maxCount != null)
            nextId = Math.max(1, maxCount);
        super.visitJavaFile(file);
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        var klass = TranspileUtils.getNewExpressionClass(expression);
        if(klass != null) {
            var replacementName = map.get((klass));
            if(replacementName != null) {
                var args = new ArrayList<String>();
                for (var arg : requireNonNull(expression.getArgumentList()).getExpressions()) {
                    args.add(arg.getText());
                }
                replace(
                        expression,
                        TranspileUtils.createExpressionFromText("new " + replacementName +
                                 "(" + NncUtils.join(args) + ")"
                        )
                );
            }
        }
    }

    @Override
    public void visitAnonymousClass(PsiAnonymousClass klass) {
        super.visitAnonymousClass(klass);
        var parent = requireNonNull(TranspileUtils.findParent(klass,
                Set.of(PsiField.class, PsiMethod.class, PsiClassInitializer.class)));
        var sb = new StringBuilder();
        if(parent instanceof PsiField field) {
            sb.append("private ");
            if (TranspileUtils.isStatic(field))
                sb.append("static ");
        }
        var name = "$" + nextId++;
        sb.append("class ").append(name).append(' ');
        var baseClassType = klass.getBaseClassType();
        var baseKlass = requireNonNull(baseClassType.resolve());
        sb.append(baseKlass.isInterface() ? "implements " : "extends ");
        sb.append(baseClassType.getCanonicalText());
        sb.append("{}");
        var k = TranspileUtils.createClassFromText(sb.toString());
        for (PsiField field : klass.getFields()) {
            k.addBefore(field.copy(), null);
        }
        k.addBefore(createConstructor(k.getName(), klass), null);
        for (PsiMethod method : klass.getMethods()) {
            k.addBefore(method.copy(), null);
        }
        for (PsiClass innerClass : klass.getInnerClasses()) {
            k.addBefore(innerClass.copy(), null);
        }
        if(parent instanceof PsiField) {
            var enclosingClass = TranspileUtils.getParentNotNull(klass.getParent(), PsiClass.class);
            enclosingClass.addBefore(k, null);
        } else {
            var stmt = requireNonNull(TranspileUtils.findParent(klass, PsiStatement.class));
            var block =  requireNonNull(stmt.getParent());
            block.addBefore(TranspileUtils.createStatementFromText(k.getText()), stmt);
        }
        map.put(klass, name);
        currentExecutable().anonymousClasses.add(klass);
    }

    private PsiMethod createConstructor(String name, PsiAnonymousClass klass) {
        var sb = new StringBuilder(name).append("(){");
        var superClass = requireNonNull(klass.getSuperClass());
        if(!TranspileUtils.isObjectClass(superClass)) {
            var argList = klass.getArgumentList();
            if (argList != null)
                sb.append("super").append(argList.getText()).append(';');
        }
        sb.append('}');
        return TranspileUtils.createMethodFromText(sb.toString());
    }

    @Override
    public void visitMethod(PsiMethod method) {
        enterExecutable();
        super.visitMethod(method);
        exitExecutable();
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        enterExecutable();
        super.visitLambdaExpression(expression);
        exitExecutable();
    }

    @Override
    public void visitClassInitializer(PsiClassInitializer initializer) {
        enterExecutable();
        super.visitClassInitializer(initializer);
        exitExecutable();
    }

    private void enterExecutable() {
        executables.push(new ExecutableInfo());
    }

    private void exitExecutable() {
        var info = requireNonNull(executables.pop());
        info.anonymousClasses.forEach(PsiElement::delete);
    }

    private ExecutableInfo currentExecutable() {
        return requireNonNull(executables.peek());
    }

    private static class ExecutableInfo {
        private final List<PsiClass> anonymousClasses = new ArrayList<>();
    }

}
