package org.metavm.entity;

import org.metavm.object.type.CheckConstraint;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;

public class CheckConstraintDef {

    private final CheckConstraint checkConstraint;
    private final Field constraintDefField;
    private final ConstraintDef<?> constraintDef;

    public CheckConstraintDef(CheckConstraint checkConstraint, Field constraintDefField, PojoDef<?> declaringTypeDef) {
        this.checkConstraint = checkConstraint;
        this.constraintDefField = constraintDefField;
        constraintDef = (ConstraintDef<?>) ReflectionUtils.get(null, constraintDefField);
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
