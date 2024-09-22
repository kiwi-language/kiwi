package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class AnonymousClassTransformer extends VisitorBase {

    private final Map<PsiAnonymousClass, AnonymousClassInfo> map = new HashMap<>();
    private final LinkedList<AnonymousClassInfo> anonymousClasses = new LinkedList<>();
    private int nextId = 1;

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        var aClass = expression.getAnonymousClass();
        if(aClass != null) {
            var info = requireNonNull(map.get(aClass));
            var args = new ArrayList<String>();
            for (var arg : requireNonNull(expression.getArgumentList()).getExpressions()) {
                args.add(arg.getText());
            }
            for (PsiVariable capturedVariable : info.capturedVariables) {
                args.add(capturedVariable.getName());
            }
            replace(
                    expression,
                    TranspileUtils.createExpressionFromText("new " + info.getSubstitutorName()
                            + "(" + NncUtils.join(args) + ")"
                    )
            );
        }
    }

    @Override
    public void visitAnonymousClass(PsiAnonymousClass aClass) {
        enterAnonymousClass(aClass);
        super.visitAnonymousClass(aClass);
        var info = exitAnonymousClass();
        var enclosingClass = TranspileUtils.getParentNotNull(aClass.getParent(), PsiClass.class);
        var baseClassType = aClass.getBaseClassType();
        var baseKlass = requireNonNull(baseClassType.resolve());
        var k = TranspileUtils.createClassFromText("class " + info.getSubstitutorName()
                + (baseKlass.isInterface() ? " implements " : " extends ")
                + baseClassType.getCanonicalText() + "{}");
        for (PsiField field : aClass.getFields()) {
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
        k.addBefore(createConstructor(info), null);
        for (PsiMethod method : aClass.getMethods()) {
            k.addBefore(method.copy(), null);
        }
        for (PsiClass innerClass : aClass.getInnerClasses()) {
            k.addBefore(innerClass.copy(), null);
        }
       enclosingClass.addBefore(k, null);
        map.put(aClass, info);
    }

    private PsiMethod createConstructor(AnonymousClassInfo anonymousClassInfo) {
        var sb = new StringBuilder(anonymousClassInfo.getSubstitutorName());
        var argList = anonymousClassInfo.klass.getArgumentList();
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

    private record ParamInfo(String name, PsiType type) {}

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

    private boolean isCapturedVariable(PsiAnonymousClass klass, PsiVariable variable) {
        var callable = TranspileUtils.getParent(variable, Set.of(PsiMethod.class, PsiLambdaExpression.class));
        var r =  TranspileUtils.isAncestor(klass, callable);
        return r;
    }

    private void enterAnonymousClass(PsiAnonymousClass klass) {
        anonymousClasses.push(new AnonymousClassInfo(klass, "$" + nextId++));
    }

    private AnonymousClassInfo exitAnonymousClass() {
        return anonymousClasses.pop();
    }

    private @Nullable AnonymousClassInfo currentAnonymousClass() {
        return anonymousClasses.peek();
    }

    private static class AnonymousClassInfo {
        private final PsiAnonymousClass klass;
        private final Set<PsiVariable> capturedVariables = new LinkedHashSet<>();
        private final String substitutorName;

        private AnonymousClassInfo(PsiAnonymousClass klass, String substitutorName) {
            this.klass = klass;
            this.substitutorName = substitutorName;
        }

        public PsiAnonymousClass getKlass() {
            return klass;
        }

        public String getSubstitutorName() {
            return substitutorName;
        }

    }

}
