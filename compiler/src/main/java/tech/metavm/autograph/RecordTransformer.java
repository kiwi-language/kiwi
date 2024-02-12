package tech.metavm.autograph;

import com.intellij.psi.*;

public class RecordTransformer extends VisitorBase {

    @Override
    public void visitClass(PsiClass psiClass) {
        if (psiClass.isRecord()) {
            var classTextBuf = new StringBuilder();
            for (PsiAnnotation annotation : psiClass.getAnnotations()) {
                classTextBuf.append(annotation.getText()).append("\n");
            }
            if(TranspileUtil.isPublic(psiClass))
                classTextBuf.append("public ");
            else if(TranspileUtil.isPrivate(psiClass))
                classTextBuf.append("private ");
            else if(TranspileUtil.isProtected(psiClass))
                classTextBuf.append("protected ");
            classTextBuf.append("class ").append(psiClass.getName()).append(" {}");
            var klass = TranspileUtil.createClassFromText(classTextBuf.toString());
            super.visitClass(psiClass);
            for (PsiRecordComponent recordComponent : psiClass.getRecordComponents()) {
                var fieldTextBuf = new StringBuilder();
                for (PsiAnnotation annotation : recordComponent.getAnnotations()) {
                    fieldTextBuf.append(annotation.getText()).append("\n");
                }
                fieldTextBuf.append("private final ").append(recordComponent.getType().getCanonicalText()).append(" ").append(recordComponent.getName()).append(";");
                klass.addBefore(TranspileUtil.createFieldFromText(fieldTextBuf.toString()), null);
//                klass.addBefore(
//                        TranspileUtil.createGetter(recordComponent.getName(), recordComponent.getType()),
//                        null);
            }
            for (PsiMethod method : psiClass.getMethods()) {
                klass.addBefore(
                        TranspileUtil.createMethodFromText(method.getText()), null);
            }
            for (PsiClass innerClass : psiClass.getInnerClasses()) {
                klass.addBefore(innerClass, null);
            }
//            if(getCanonicalConstructor(psiClass) == null) {
//                var buf = new StringBuilder("public ").append(psiClass.getName()).append("(");
//                for (int i = 0; i < psiClass.getRecordComponents().length; i++) {
//                    if (i > 0) buf.append(", ");
//                    buf.append(psiClass.getRecordComponents()[i].getType().getCanonicalText()).append(" ").append(psiClass.getRecordComponents()[i].getName());
//                }
//                buf.append(") {");
//                for (int i = 0; i < psiClass.getRecordComponents().length; i++) {
//                    buf.append("this.").append(psiClass.getRecordComponents()[i].getName()).append(" = ").append(psiClass.getRecordComponents()[i].getName()).append(";");
//                }
//                buf.append("}");
//                klass.addBefore(TranspileUtil.createMethodFromText(buf.toString()), null);
//            }
            replace(psiClass, klass);
        }
    }

    private PsiMethod getCanonicalConstructor(PsiClass psiClass) {
        for (PsiMethod method : psiClass.getMethods()) {
            if (method.isConstructor() && method.getParameterList().getParametersCount() == psiClass.getRecordComponents().length &&
                    method.getBody() != null) {
                var statements = method.getBody().getStatements();
                if (statements.length == 1 &&
                        statements[0] instanceof PsiCallExpression &&
                        statements[0].getText().trim().startsWith("this"))
                    return method;
            }
        }
        return null;
    }

}
