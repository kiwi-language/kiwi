package tech.metavm.entity;

import tech.metavm.object.type.Index;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;

public class IndexConstraintDef {

    private final Index indexConstraint;
    private final Field indexDefField;
    private final IndexDef<?> indexDef;

    public IndexConstraintDef(Index indexConstraint, Field indexDefField, PojoDef<?> declaringTypeDef) {
        this.indexConstraint = indexConstraint;
        this.indexDefField = indexDefField;
        indexDef = (IndexDef<?>) ReflectUtils.get(null, indexDefField);
        indexConstraint.setIndexDef(indexDef);
        declaringTypeDef.addUniqueConstraintDef(this);
    }

    public Index getIndexConstraint() {
        return indexConstraint;
    }

    public Field getIndexDefField() {
        return indexDefField;
    }

    public IndexDef<?> getIndexDef() {
        return indexDef;
    }
}
