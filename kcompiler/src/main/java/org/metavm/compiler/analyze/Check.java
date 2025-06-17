package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.Errors;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.syntax.ClassParamDecl;
import org.metavm.compiler.syntax.FieldDecl;
import org.metavm.compiler.syntax.StructuralNodeVisitor;
import org.metavm.compiler.type.Types;

public class Check extends StructuralNodeVisitor {

    private final Log log;

    public Check(Log log) {
        this.log = log;
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        var field = fieldDecl.getElement();
        if (field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
            log.error(fieldDecl, Errors.summaryFieldMustBeString);
        return super.visitFieldDecl(fieldDecl);
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        var field = classParamDecl.getField();
        if (field != null && field.getDeclClass().getSummaryField() == field && field.getType() != Types.instance.getStringType())
            log.error(classParamDecl, Errors.summaryFieldMustBeString);
        return super.visitClassParamDecl(classParamDecl);
    }
}
