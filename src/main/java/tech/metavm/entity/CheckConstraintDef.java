package tech.metavm.entity;

import tech.metavm.object.meta.CheckConstraint;
import tech.metavm.object.meta.Index;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;

public class CheckConstraintDef {

    private final CheckConstraint checkConstraint;
    private final Field constraintDefField;
    private final ConstraintDef<?> constraintDef;

    public CheckConstraintDef(CheckConstraint checkConstraint, Field constraintDefField, PojoDef<?> declaringTypeDef) {
        this.checkConstraint = checkConstraint;
        this.constraintDefField = constraintDefField;
        constraintDef = (ConstraintDef<?>) ReflectUtils.get(null, constraintDefField);
        checkConstraint.setConstraintDef(constraintDef);
        declaringTypeDef.addCheckConstraintDef(this);
    }

    public CheckConstraint getCheckConstraint() {
        return checkConstraint;
    }

    public Field getConstraintDefField() {
        return constraintDefField;
    }

    public ConstraintDef<?> getConstraintDef() {
        return constraintDef;
    }
}
