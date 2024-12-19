package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

import java.util.*;

import static org.metavm.autograph.TranspileUtils.createAnnotationFromText;

@Slf4j
public class EnumTransformer extends VisitorBase {

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        if (aClass.isEnum()) {
            var isAbstract = NncUtils.anyMatch(List.of(aClass.getMethods()), TranspileUtils::isAbstract);
            var mods = Objects.requireNonNull(aClass.getModifierList()).getText()
                    + (aClass.getContainingClass() == null ||
                            aClass.getModifierList().hasExplicitModifier(PsiModifier.STATIC) ? "" : " static")
                    + (isAbstract ? " abstract" : "")
                    ;
            var text = mods + " class " + aClass.getName() + " extends java.lang.Enum<" + aClass.getName() + "> {}";
            var newClass = TranspileUtils.createClassFromText(text);
            int ordinal = 0;
            var enumConstantNames = new LinkedHashSet<String>();
            for (PsiField field : aClass.getFields()) {
                if (field instanceof PsiEnumConstant enumConstant) {
                    enumConstantNames.add(field.getName());
                    var argList = enumConstant.getArgumentList() != null ?
                            NncUtils.join(enumConstant.getArgumentList().getExpressions(), PsiElement::getText) : "";
                    var fieldText = "public static final "
                            + aClass.getName() + " "
                            + enumConstant.getName() + " = new " + aClass.getName()
                            + "(\"" + enumConstant.getName() + "\", " + ordinal++
                            + (argList.isEmpty() ? "" : ", " + argList)
                            + ")"
                            + (
                            enumConstant.getInitializingClass() != null ?
                                    enumConstant.getInitializingClass().getText() : ""
                    ) + ";";
                    newClass.addBefore(
                            TranspileUtils.createFieldFromText(fieldText),
                            null
                    );
                } else
                    newClass.addBefore(field.copy(), null);
            }
            boolean hasConstructor = false;
            for (PsiMethod method : aClass.getMethods()) {
                if (method instanceof SyntheticElement)
                    continue;
                method = (PsiMethod) method.copy();
                if (method.isConstructor()) {
                    hasConstructor = true;
                    method.getParameterList().addAfter(
                            TranspileUtils.createParameterFromText("int $ordinal"), null
                    );
                    method.getParameterList().addAfter(
                            TranspileUtils.createParameterFromText("String $name"), null
                    );
                    var body = Objects.requireNonNull(method.getBody());
                    body.addAfter(
                            TranspileUtils.createStatementFromText("super($name, $ordinal);"),
                            null
                    );
                }
                newClass.addBefore(method, null);
            }
            newClass.addBefore(createValuesMethod(aClass, enumConstantNames), null);
            if (!hasConstructor) {
                newClass.addBefore(
                        TranspileUtils.createMethodFromText(
                                aClass.getName() + "(String $name, int $ordinal) {super($name, $ordinal);}"
                        ),
                        null
                );
            }
            for (PsiClass innerClass : aClass.getInnerClasses()) {
                newClass.addBefore(innerClass.copy(), null);
            }
            Objects.requireNonNull(newClass.getModifierList()).addAfter(
                    createAnnotationFromText("@org.metavm.api.Enum"), null
            );
            newClass = (PsiClass) replace(aClass, newClass);
            var enumConstants = new ArrayList<PsiField>();
            newClass.putUserData(Keys.ENUM_CONSTANTS, enumConstants);
            ordinal = 0;
            for (PsiField field : newClass.getFields()) {
                if (enumConstantNames.contains(field.getName())) {
                    Objects.requireNonNull(field.getModifierList()).addAfter(
                            createAnnotationFromText("@org.metavm.api.EnumConstant(ordinal=" + ordinal++  + ")"),
                            null
                    );
                    enumConstants.add(field);
                    var initializer = (PsiNewExpression) Objects.requireNonNull(field.getInitializer());
                    if (initializer.getAnonymousClass() != null)
                        initializer.getAnonymousClass().putUserData(Keys.ANONYMOUS_CLASS_NAME, "$" + field.getName());
                }
            }
//            log.debug("{}", newClass.getText());
        }
    }

    private PsiMethod createValuesMethod(PsiClass enumClass, Collection<String> enumConstantNames) {
        return TranspileUtils.createMethodFromText("public static " + enumClass.getName() + "[] values() {"
                + "return "
                + "new " + enumClass.getName()
                + "[] {" + NncUtils.join(enumConstantNames) + "};"
                + "}");
    }

}
