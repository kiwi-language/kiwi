package org.metavm.autograph;

import com.intellij.psi.*;

public class RecordTransformer extends VisitorBase {

    @Override
    public void visitClass(PsiClass psiClass) {
        if (psiClass.isRecord()) {
            var classTextBuf = new StringBuilder();
            for (PsiAnnotation annotation : psiClass.getAnnotations()) {
                classTextBuf.append(annotation.getText()).append("\n");
            }
            if(TranspileUtils.isPublic(psiClass))
                classTextBuf.append("public ");
            else if(TranspileUtils.isPrivate(psiClass))
                classTextBuf.append("private ");
            else if(TranspileUtils.isProtected(psiClass))
                classTextBuf.append("protected ");
            classTextBuf.append("class ").append(psiClass.getName()).append(" {}");
            var klass = TranspileUtils.createClassFromText(classTextBuf.toString());
            super.visitClass(psiClass);
            for (PsiRecordComponent recordComponent : psiClass.getRecordComponents()) {
                var fieldTextBuf = new StringBuilder();
                for (PsiAnnotation annotation : recordComponent.getAnnotations()) {
                    fieldTextBuf.append(annotation.getText()).append("\n");
                }
                fieldTextBuf.append("private final ").append(recordComponent.getType().getCanonicalText()).append(" ").append(recordComponent.getName()).append(";");
                klass.addBefore(TranspileUtils.createFieldFromText(fieldTextBuf.toString()), null);
            }
            for (PsiMethod method : psiClass.getMethods()) {
                klass.addBefore(method.copy(), null);}
            for (PsiClass innerClass : psiClass.getInnerClasses()) {
                klass.addBefore(innerClass.copy(), null);
            }
            replace(psiClass, klass);
        }
    }

}
