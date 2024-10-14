package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class AnonymousClassTransformer extends VisitorBase {

    private final Map<PsiClass, AnonymousClassInfo> map = new HashMap<>();
    private final LinkedList<AnonymousClassInfo> anonymousClasses = new LinkedList<>();
    private int nextId = 1;

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        var klass = TranspileUtils.getNewExpressionClass(expression);
        if(klass != null) {
            var info = map.get((klass));
            if(info != null) {
                var args = new ArrayList<String>();
                for (var arg : requireNonNull(expression.getArgumentList()).getExpressions()) {
                    args.add(arg.getText());
                }
                for (PsiVariable capturedVariable : info.capturedVariables) {
                    args.add(capturedVariable.getName());
                }
                String typeArgs = info.capturedTypeVariables.isEmpty() ? "" :
                        "<" + NncUtils.join(info.capturedTypeVariables, PsiNamedElement::getName) + ">";
                replace(
                        expression,
                        TranspileUtils.createExpressionFromText("new " + info.getSubstitutorName()
                                + typeArgs + "(" + NncUtils.join(args) + ")"
                        )
                );
            }
        }
    }

    @Override
    public void visitAnonymousClass(PsiAnonymousClass aClass) {
        enterAnonymousClass(aClass);
        super.visitAnonymousClass(aClass);
        processLocalClass(exitAnonymousClass());
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if(TranspileUtils.isLocalClass(aClass)) {
            enterAnonymousClass(aClass);
            super.visitClass(aClass);
            processLocalClass(exitAnonymousClass());
            replace(aClass, TranspileUtils.createComment("// Removed"));
        }
        else
            super.visitClass(aClass);
    }

    private void processLocalClass(AnonymousClassInfo info) {
        var klass = info.klass;
        var enclosingClass = TranspileUtils.getParentNotNull(klass.getParent(), PsiClass.class);
        String extensionText;
        if(klass instanceof PsiAnonymousClass anonymousClass) {
            var baseClassType = anonymousClass.getBaseClassType();
            var baseKlass = requireNonNull(baseClassType.resolve());
            extensionText = (baseKlass.isInterface() ? " implements " : " extends ")
                    + baseClassType.getCanonicalText();
        }
        else {
            extensionText = " " + Objects.requireNonNull(klass.getExtendsList()).getText()
                    + " " + Objects.requireNonNull(klass.getImplementsList()).getText();
        }
        var parent = TranspileUtils.getParent(klass, Set.of(PsiField.class, PsiMethod.class));
        var nonStatic = parent instanceof PsiField field && !TranspileUtils.isStatic(field)
                || parent instanceof PsiMethod method && !TranspileUtils.isStatic(method);
        String typeParameters;
        if(info.capturedTypeVariables.isEmpty())
            typeParameters = "";
        else {
            typeParameters = "<" + NncUtils.join(info.capturedTypeVariables, PsiNamedElement::getName) + ">";
        }
        var k = TranspileUtils.createClassFromText((nonStatic ? "private class " : "private static class ")
                + info.getSubstitutorName()
                + typeParameters
                +  extensionText + "{}");
        for (PsiField field : klass.getFields()) {
            var f = (PsiField) field.copy();
            f.setInitializer(null);
            k.addBefore(f, null);
        }
        for (PsiVariable v : info.capturedVariables) {
            k.addBefore(
                    TranspileUtils.createFieldFromText(
                            "private final " + v.getType().getCanonicalText() + " " + v.getName() + ";"
                    ),
                    null
            );
        }
        if(klass instanceof PsiAnonymousClass)
            k.addBefore(createConstructor(info), null);
        for (PsiMethod method : klass.getMethods()) {
            var m = (PsiMethod) k.addBefore(method.copy(), null);
            if(m.isConstructor())
                transformLocalClassConstructor(m, info);
        }
        for (PsiClass innerClass : klass.getInnerClasses()) {
            k.addBefore(innerClass.copy(), null);
        }
        k = (PsiClass) enclosingClass.addBefore(k, null);
        map.put(klass instanceof PsiAnonymousClass ? klass : k, info);
    }

    private void transformLocalClassConstructor(PsiMethod method, AnonymousClassInfo info) {
        var paramList = Objects.requireNonNull(method.getParameterList());
        var scope = Objects.requireNonNull(info.klass.getUserData(Keys.BODY_SCOPE));
        var definedVars = scope.getAllDefined();
        var capturedVariableMap = new HashMap<PsiVariable, ParamInfo>();
        for (PsiVariable v : info.capturedVariables) {
            var param = new ParamInfo(namer.newName("a" + capturedVariableMap.size(), definedVars), v.getType());
            capturedVariableMap.putIfAbsent(v, param);
        }
        var body = Objects.requireNonNull(method.getBody());
        var statements = body.getStatements();
        for (ParamInfo param : capturedVariableMap.values()) {
            paramList.add(param.createParameter());
        }
        if(statements.length > 0 && TranspileUtils.isThisCall(statements[0])) {
            var call = (PsiMethodCallExpression) ((PsiExpressionStatement) statements[0]).getExpression();
            var args = Objects.requireNonNull(call.getArgumentList());
            for (ParamInfo param : capturedVariableMap.values()) {
                args.add(TranspileUtils.createExpressionFromText(param.name()));
            }
        } else {
            PsiElement anchor = null;
            if (statements.length > 0 && TranspileUtils.isSuperCall(statements[0]))
                anchor = body.getStatements()[0];
            for (var e : capturedVariableMap.entrySet()) {
                var v = e.getKey();
                var param = e.getValue();
                anchor = body.addAfter(
                        TranspileUtils.createStatementFromText("this." + v.getName() + "=" + param.name + ";"),
                        anchor
                );
            }
            for (PsiField field : info.klass.getFields()) {
                if(field.getInitializer() != null)
                    body.add(TranspileUtils.createStatementFromText(
                            "this." + field.getName() + "=" + field.getInitializer().getText() + ";"
                    ));
            }
        }
    }

    private PsiMethod createConstructor(AnonymousClassInfo anonymousClassInfo) {
        var sb = new StringBuilder(anonymousClassInfo.getSubstitutorName());
        var argList = ((PsiAnonymousClass) anonymousClassInfo.klass).getArgumentList();
        var params = new ArrayList<ParamInfo>();
        var scope = Objects.requireNonNull(anonymousClassInfo.klass.getUserData(Keys.BODY_SCOPE));
        var definedVars = scope.getAllDefined();
        var superCallParams = new ArrayList<ParamInfo>();
        if(argList != null) {
            for (PsiExpression arg : argList.getExpressions()) {
                var param = new ParamInfo(
                        namer.newName("a" + params.size(), definedVars),
                        requireNonNull(arg.getType())
                );
                params.add(param);
                superCallParams.add(param);
            }
        }
        var capturedVarParamMap = new HashMap<PsiVariable, ParamInfo>();
        for (PsiVariable v : anonymousClassInfo.capturedVariables) {
            var param = new ParamInfo(namer.newName("a" + params.size(), definedVars), v.getType());
            params.add(param);
            capturedVarParamMap.put(v, param);
        }
        sb.append('(')
                .append(NncUtils.join(params, p -> p.type.getCanonicalText() + " " + p.name()))
                .append(')');
        sb.append('{');
        if (!superCallParams.isEmpty()) {
           sb.append("super(").append(NncUtils.join(superCallParams, ParamInfo::name)).append(");");
        }
        capturedVarParamMap.forEach((v ,param) ->
                sb.append("this.").append(v.getName()).append('=').append(param.name()).append(';')
        );
        for (PsiField field : anonymousClassInfo.getKlass().getFields()) {
            if(!TranspileUtils.isStatic(field) && field.getInitializer() != null) {
                sb.append("this.").append(field.getName()).append('=')
                        .append(field.getInitializer().getText()).append(';');
            }
        }
        sb.append('}');
        return TranspileUtils.createMethodFromText(sb.toString());
    }

    private record ParamInfo(String name, PsiType type) {

        PsiParameter createParameter() {
            return TranspileUtils.createParameterFromText(type.getCanonicalText() + " " + name);
        }

    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        super.visitReferenceExpression(expression);
        if(expression.resolve() instanceof PsiVariable variable && !(variable instanceof PsiField)) {
            for (AnonymousClassInfo anonymousClass : anonymousClasses) {
                if(isCapturedVariable(anonymousClass.klass, variable))
                    anonymousClass.capturedVariables.add(variable);
                else
                    break;
            }
        }
    }

    private boolean isCapturedVariable(PsiClass klass, PsiVariable variable) {
        var callable = TranspileUtils.getParent(variable, Set.of(PsiMethod.class, PsiLambdaExpression.class));
        return TranspileUtils.isAncestor(klass, callable);
    }

    @Override
    public void visitTypeElement(PsiTypeElement type) {
        super.visitTypeElement(type);
        if(type.getType() instanceof PsiClassType classType && classType.resolve() instanceof PsiTypeParameter typeVar) {
            for (AnonymousClassInfo anonymousClass : anonymousClasses) {
                if(isCapturedTypeVariable(anonymousClass.klass, typeVar))
                    anonymousClass.capturedTypeVariables.add(typeVar);
                else
                    break;
            }
        }
    }

    private boolean isCapturedTypeVariable(PsiClass klass, PsiTypeParameter typeParameter) {
        return typeParameter.getOwner() instanceof PsiMethod method && TranspileUtils.isAncestor(klass, method);
    }

    private void enterAnonymousClass(PsiClass klass) {
        var substName = klass instanceof PsiAnonymousClass ? "$" + nextId++ : klass.getName();
        anonymousClasses.push(new AnonymousClassInfo(klass, substName));
    }

    private AnonymousClassInfo exitAnonymousClass() {
        return anonymousClasses.pop();
    }

    private @Nullable AnonymousClassInfo currentAnonymousClass() {
        return anonymousClasses.peek();
    }

    private static class AnonymousClassInfo {
        private final PsiClass klass;
        private final Set<PsiVariable> capturedVariables = new LinkedHashSet<>();
        private final Set<PsiTypeParameter> capturedTypeVariables = new LinkedHashSet<>();
        private final String substitutorName;

        private AnonymousClassInfo(PsiClass klass, String substitutorName) {
            this.klass = klass;
            this.substitutorName = substitutorName;
        }

        public PsiClass getKlass() {
            return klass;
        }

        public String getSubstitutorName() {
            return substitutorName;
        }

    }

}
