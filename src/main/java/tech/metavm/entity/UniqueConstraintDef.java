package tech.metavm.entity;

import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;

public class UniqueConstraintDef {

    private final UniqueConstraintRT uniqueConstraint;
    private final Field indexDefField;
    private final IndexDef<?> indexDef;
    private final PojoDef<?> declaringTypeDef;

    public UniqueConstraintDef(UniqueConstraintRT uniqueConstraint, Field indexDefField, PojoDef<?> declaringTypeDef) {
        this.uniqueConstraint = uniqueConstraint;
        this.indexDefField = indexDefField;
        this.declaringTypeDef = declaringTypeDef;
        indexDef = (IndexDef<?>) ReflectUtils.get(null, indexDefField);
        uniqueConstraint.setIndexDef(indexDef);
        declaringTypeDef.addUniqueConstraintDef(this);
    }

    public UniqueConstraintRT getUniqueConstraint() {
        return uniqueConstraint;
    }

    public Field getIndexDefField() {
        return indexDefField;
    }

    public IndexDef<?> getIndexDef() {
        return indexDef;
    }
}
