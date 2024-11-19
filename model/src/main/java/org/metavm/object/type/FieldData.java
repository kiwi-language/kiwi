package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Column;

import javax.annotation.Nullable;

@EntityType
public class FieldData extends Entity {

    public static final IndexDef<FieldData> IDX_DECLARING_TYPE = IndexDef.create(FieldData.class, "declaringType");

    private final String name;
    private final Column column;
    private final boolean unique;
    private final Klass declaringType;
    private final Access access;
    private final Type type;
    private final boolean isChild;
    private final boolean isStatic;
    private final Value staticValue;
    @Nullable
    private final Value defaultValue;

    public FieldData(Long tmpId, String name, Column column, boolean unique, Klass declaringType, Access access, Type type, boolean isChild, boolean isStatic, Value staticValue, @Nullable Value defaultValue) {
        super(tmpId);
        this.name = name;
        this.column = column;
        this.unique = unique;
        this.declaringType = declaringType;
        this.access = access;
        this.type = type;
        this.isChild = isChild;
        this.isStatic = isStatic;
        this.staticValue = staticValue;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Column getColumn() {
        return column;
    }

    public boolean isUnique() {
        return unique;
    }

    public Klass getDeclaringType() {
        return declaringType;
    }

    public Access getAccess() {
        return access;
    }

    public Type getType() {
        return type;
    }

    public boolean isChild() {
        return isChild;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Value getStaticValue() {
        return staticValue;
    }

    @Nullable
    public Value getDefaultValue() {
        return defaultValue;
    }

}
