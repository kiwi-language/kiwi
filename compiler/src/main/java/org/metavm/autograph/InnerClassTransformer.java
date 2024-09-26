package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.IdentitySet;
import org.metavm.util.LinkedList;
import org.metavm.util.NncUtils;

import java.util.*;

@Slf4j
public class InnerClassTransformer extends VisitorBase {

    public static final String PARENT = "$parent$";
    private final LinkedList<PsiClass> klasses = new LinkedList<>();
    private final Map<PsiJavaCodeReferenceElement, TypeText> typeTextMap = new IdentityHashMap<>();
    private final Set<PsiClass> preResolved = new IdentitySet<>();

    @Override
    public void visitClass(PsiClass psiClass) {
        if(TranspileUtils.isDiscarded(psiClass))
            return;
        psiClass.accept(new PreResolver());
        var isNonStaticInner = TranspileUtils.isNonStaticInnerClass(psiClass);
        klasses.push(psiClass);
        if(isNonStaticInner) {
            var outerClass = Objects.requireNonNull(psiClass.getContainingClass());
            var sb = new StringBuilder("private final ");
            /*
            There's a bug with the Psi library.
            Type resolution will fail if the type of the parent field is qualified.
             */
            sb.append(outerClass.getName());
            if(outerClass.getTypeParameters().length > 0) {
                sb.append('<');
                for (int i = 0; i < outerClass.getTypeParameters().length; i++) {
                    var typeParameter = outerClass.getTypeParameters()[i];
                    if(i > 0)
                        sb.append(", ");
                    sb.append(typeParameter.getName());
                }
                sb.append('>');
            }
            sb.append(' ').append(PARENT).append(';');
            var fieldText = sb.toString();
            var parentField = TranspileUtils.createFieldFromText(fieldText);
            psiClass.addAfter(parentField, null);
        }
        super.visitClass(psiClass);
        if (isNonStaticInner) {
            var outerClass = psiClass.getContainingClass();
            while (outerClass != null) {
                for (int i = outerClass.getTypeParameters().length - 1; i >= 0; i--) {
                    var typeParameter = outerClass.getTypeParameters()[i];
                    var typeParamList = Objects.requireNonNull(psiClass.getTypeParameterList());
                    typeParamList.addAfter(
                            TranspileUtils.createTypeParameter(typeParameter.getName(), typeParameter.getSuperTypes()),
                            null
                    );
                }
                if(TranspileUtils.isStatic(outerClass))
                    break;
                outerClass = outerClass.getContainingClass();
            }
            Objects.requireNonNull(psiClass.getModifierList()).setModifierProperty(PsiModifier.STATIC, true);
            psiClass.putUserData(Keys.ADDED_STATIC_MODIFIER, true);
            for (PsiField field : psiClass.getFields()) {
                if(!TranspileUtils.isStatic(field))
                    field.setInitializer(null);
            }
        }
        klasses.pop();
    }

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        if (method.isConstructor()) {
            var declaringClass = Objects.requireNonNull(method.getContainingClass());
            if (TranspileUtils.isNonStaticInnerClass(declaringClass)) {
                var outerClass = declaringClass.getContainingClass();
                var paramList = method.getParameterList();
                var text = getSimpleTypeText(outerClass) + " " + PARENT;
                var parentParam = TranspileUtils.createParameterFromText(text);
                paramList.addAfter(parentParam, null);
                var body = Objects.requireNonNull(method.getBody());
                PsiElement anchor = null;
                boolean selfConstructorCalled = false;
                if (body.getStatements().length > 0) {
                    var firstStmt = body.getStatements()[0];
                    if(firstStmt instanceof PsiExpressionStatement exprStmt && exprStmt.getExpression() instanceof PsiMethodCallExpression callExpr) {
                        var refName = callExpr.getMethodExpression().getReferenceName();
                        if("this".equals(refName))
                            selfConstructorCalled = true;
                        else if("super".equals(refName))
                            anchor = firstStmt;
                    }
                }
                if(!selfConstructorCalled) {
                    body.addAfter(
                            TranspileUtils.createStatementFromText(String.format("this.%s = %s;", PARENT, PARENT)),
                            anchor
                    );
                }
                for (PsiField field : declaringClass.getFields()) {
                    if(!TranspileUtils.isStatic(field) && field.getInitializer() != null) {
                        body.addBefore(
                                TranspileUtils.createStatementFromText(
                                        "this." + field.getName() + " = " + field.getInitializer().getText() + ";"
                                ),
                                null
                        );
                    }
                }
            }
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        if(expression.getMethodExpression().resolve() instanceof PsiMethod method) {
            if (method.isConstructor() && TranspileUtils.isNonStaticInnerClass(Objects.requireNonNull(method.getContainingClass()))) {
                expression.getArgumentList().addAfter(
                        TranspileUtils.createExpressionFromText(PARENT),
                        null
                );
            }
        }
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (expression.getQualifier() != null) {
//            log.debug("Visiting new expression: {}", expression.getText());
            var qualifier = expression.getQualifier();
            var args = List.of(Objects.requireNonNull(expression.getArgumentList()).getExpressions());
            var replacement = TranspileUtils.createExpressionFromText(
                    "new "
                            + Objects.requireNonNull(expression.getClassOrAnonymousClassReference()).getText()
                            + "("
                            + qualifier.getText() +
                            (
                                    args.isEmpty() ? "" :
                                            ", " + NncUtils.join(args, PsiElement::getText, ", ")
                            )
                            + ")"
            );
            replace(expression, replacement);
        }
    }

    private boolean shouldTransform(PsiJavaCodeReferenceElement reference) {
        var context = reference.getContext();
        return context instanceof PsiTypeElement && !(context.getParent() instanceof PsiClassObjectAccessExpression) ||
                (context instanceof PsiNewExpression newExpr && newExpr.getClassOrAnonymousClassReference() == reference);
    }

    public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
        if(shouldTransform(reference)) {
            try {
                var text = getTypeText(reference);
                if (!text.isValid())
                    throw new IllegalStateException(String.format("Failed to get type text for reference %s in context %s",
                            reference.getText(), Objects.requireNonNull(reference.getContext()).getText()));
                replace(reference, TranspileUtils.createReferenceElement(text.text, reference.getContext()));
            }
            catch (Throwable e) {
                log.error("Failed to get type text for {} in context {}",
                        reference.getText(), Objects.requireNonNull(reference.getContext()).getText());
                log.debug("{}", Objects.requireNonNull(TranspileUtils.getAncestor(reference, PsiJavaFile.class)).getText());
                throw e;
            }
        }
        else
            super.visitReferenceElement(reference);
    }

    private TypeText getTypeText(PsiJavaCodeReferenceElement reference) {
//        if(reference.getText().equals("Item<String>"))
//            log.debug("Getting type text for {}@{}", reference.getText(), System.identityHashCode(reference), new Exception());
        return typeTextMap.computeIfAbsent(reference, this::createTypeText);
    }

    private TypeText createTypeText(PsiJavaCodeReferenceElement reference) {
        var resolveResult = reference.advancedResolve(false);
        if(resolveResult.getElement() instanceof PsiClass klass
                && !(reference.getContext() instanceof PsiThisExpression)
                && !(reference.getContext() instanceof PsiImportStatementBase)
        ) {
//            log.debug("Creating type text for reference {} in context {}", reference.getText(),
//                    Objects.requireNonNull(reference.getContext()).getText());
            return new TypeText(getClassTypeText(klass, resolveResult.getSubstitutor()));
        }
        else
            throw new IllegalStateException(String.format("Failed to get type text for reference %s in context %s",
                    reference.getText(), Objects.requireNonNull(reference.getContext()).getText()));
//        return TypeText.createInvalid();
    }

    private String getTypeText(PsiType type) {
        return switch(type) {
            case PsiClassType classType -> {
                var generics = classType.resolveGenerics();
                if(generics.getElement() == null)
                    yield classType.getCanonicalText();
                yield getClassTypeText(generics.getElement(), generics.getSubstitutor());
            }
            case PsiArrayType arrayType -> getTypeText(arrayType.getComponentType()) + "[]";
            default -> type.getCanonicalText();
        };
    }

    private String getClassTypeText(PsiClass klass, PsiSubstitutor substitutor) {
        var typeArgs = TranspileUtils.getAllTypeArgumentsForInnerClass(klass, substitutor);
        var k = Objects.requireNonNullElse(klass.getUserData(Keys.SUBSTITUTION), klass);
        var enclosingKlasses = TranspileUtils.getEnclosingClasses(k);
        var prefix = NncUtils.join(enclosingKlasses, PsiNamedElement::getName, ".");
        if(typeArgs.isEmpty())
            return prefix;
        else
            return prefix + "<" + NncUtils.join(typeArgs, this::getTypeText) + ">";
    }

    @Override
    public void visitThisExpression(PsiThisExpression expression) {
//        log.debug("Visiting this expression: {} in context {}", expression.getText(),
//                Objects.requireNonNull(expression.getContext()).getText());
        super.visitThisExpression(expression);
        if(expression.getQualifier() != null) {
            var klass = (PsiClass) Objects.requireNonNull(expression.getQualifier().resolve());
            var k = currentKlass();
            if(k != klass) {
                StringBuilder sb = new StringBuilder();
                while (k != klass) {
                    k = Objects.requireNonNull(k,
                            "current class: " + currentKlass().getName() + ", expression: " + expression.getText()).getContainingClass();
                    if (!sb.isEmpty())
                        sb.append('.');
                    sb.append(PARENT);
                }
                replace(expression, TranspileUtils.createExpressionFromText(sb.toString()));
            }
            else
                replace(expression, TranspileUtils.createExpressionFromText("this"));
        }
    }

    private String getSimpleTypeText(PsiClass psiClass) {
        var ownerKlasses = TranspileUtils.getOwnerClasses(psiClass);
        if(NncUtils.anyMatch(ownerKlasses, k -> k.getTypeParameters().length > 0)) {
            var sb = new StringBuilder(Objects.requireNonNull(psiClass.getName())).append('<');
            var f = true;
            for (PsiClass ownerKlass : ownerKlasses) {
                for (PsiTypeParameter typeParameter : ownerKlass.getTypeParameters()) {
                    if(f)
                        f = false;
                    else
                        sb.append(',');
                    sb.append(typeParameter.getName());
                }
            }
            sb.append('>');
            return sb.toString();
        }
        else
            return psiClass.getName();
    }

    private PsiClass currentKlass() {
        return Objects.requireNonNull(klasses.peek());
    }

    private record TypeText(String text) {

        public static TypeText createInvalid() {
            return new TypeText("");
        }

        boolean isValid() {
            return !text.isEmpty();
        }

    }

    private class PreResolver extends VisitorBase {

        @Override
        public void visitClass(PsiClass aClass) {
            if(preResolved.add(aClass))
                super.visitClass(aClass);
        }

        @Override
        public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
//            log.debug("Visiting reference {} in context {}, context class: {}",
//                    reference.getText(), Objects.requireNonNull(reference.getContext()).getText(),
//                    Objects.requireNonNull(reference.getContext()).getClass().getName()
//                    );
//            log.debug("Visiting reference {}", reference.getText());
            if(shouldTransform(reference)) {
                try {
                    getTypeText(reference);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to get type context for reference " + reference.getText()
                    + " in context " + NncUtils.get(reference.getContext(), PsiElement::getText), e);
                }
            }
            else
                super.visitReferenceElement(reference);
        }

    }

}
