package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class RecordToClass extends VisitorBase {

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        if(aClass.isRecord()) {
            var sb = new StringBuilder();
            if(aClass.getModifierList() != null)
                sb.append(aClass.getModifierList().getText()).append(' ');
            if(aClass.getContainingClass() != null)
                sb.append("static ");
            sb.append("class ").append(aClass.getName());
            if(aClass.getTypeParameterList() != null) {
                var typeParams = aClass.getTypeParameterList().getTypeParameters();
                if(typeParams.length > 0) {
                    sb.append('<');
                    sb.append(typeParams[0].getText());
                    for (int i = 1; i < typeParams.length; i++) {
                        sb.append(',').append(typeParams[i].getText());
                    }
                    sb.append('>');
                }
            }
            if(aClass.getImplementsList() != null) {
                sb.append(" ").append(aClass.getImplementsList().getText());
            }
            sb.append("{");
            var componentMap = new HashMap<String, PsiRecordComponent>();
            for (PsiRecordComponent rc : aClass.getRecordComponents()) {
                componentMap.put(rc.getName(), rc);
            }
            for (PsiField field : aClass.getFields()) {
                var rc = componentMap.get(field.getName());
                if(rc != null) {
                    for (PsiAnnotation annotation : rc.getAnnotations()) {
                        sb.append(annotation.getText()).append('\n');
                    }
                }
                sb.append(field.getText());
            }
            for (PsiMethod method : aClass.getMethods()) {
                sb.append(method.getText());
            }

            for (PsiClass innerClass : aClass.getInnerClasses()) {
                sb.append(innerClass.getText());
            }
            sb.append("}");
            var text = sb.toString();
            replace(aClass, TranspileUtils.createClassFromText(text));
        }
    }
}
