package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class AnonymousClassTransformer extends VisitorBase {

    private final Map<PsiClass, String> map = new HashMap<>();
    private final LinkedList<Scope> scopes = new LinkedList<>();
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
        var name = klass.getUserData(Keys.ANONYMOUS_CLASS_NAME);
        if (name == null)
            name = "$" + nextId++;
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
        currentScope().anonymousClasses.add(klass);
    }

    private PsiMethod createConstructor(String name, PsiAnonymousClass klass) {
        var sb = new StringBuilder(name).append("(");
        var args = Objects.requireNonNull(klass.getArgumentList()).getExpressions();
        for (int i = 0; i < args.length; i++) {
            var arg = args[i];
            if (i > 0)
                sb.append(',');
            sb.append(Objects.requireNonNull(arg.getType()).getCanonicalText()).append(" arg").append(i);
        }
        sb.append("){");
        var superClass = requireNonNull(klass.getSuperClass());
        if(!TranspileUtils.isObjectClass(superClass)) {
            sb.append("super(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0)
                    sb.append(',');
                sb.append("arg").append(i);
            }
            sb.append(");");
        }
        sb.append('}');
        return TranspileUtils.createMethodFromText(sb.toString());
    }

    @Override
    public void visitClass(PsiClass aClass) {
        enterScope();
        super.visitClass(aClass);
        exitScope();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        enterScope();
        super.visitMethod(method);
        exitScope();
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        enterScope();
        super.visitLambdaExpression(expression);
        exitScope();
    }

    @Override
    public void visitClassInitializer(PsiClassInitializer initializer) {
        enterScope();
        super.visitClassInitializer(initializer);
        exitScope();
    }

    private void enterScope() {
        scopes.push(new Scope());
    }

    private void exitScope() {
        requireNonNull(scopes.pop()).anonymousClasses.forEach(PsiElement::delete);
    }

    private Scope currentScope() {
        return requireNonNull(scopes.peek());
    }

    private static class Scope {
        private final List<PsiClass> anonymousClasses = new ArrayList<>();
    }

}
