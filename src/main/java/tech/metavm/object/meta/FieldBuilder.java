package tech.metavm.object.meta;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.util.Column;

import javax.annotation.Nullable;

public class FieldBuilder {

    public static FieldBuilder newBuilder(String name, @Nullable String code, ClassType declaringType, Type type) {
        return new FieldBuilder(name, code, declaringType, type);
    }

    private final String name;
    private final @Nullable String code;
    private final ClassType declaringType;
    private final Type type;
    private Column column;
    private Long tmpId;
    private Access access = Access.PUBLIC;
    private boolean unique = false;
    private boolean asTitle = false;
    private PrimitiveType nullType;
    private Instance defaultValue;
    private boolean isChild;
    private boolean isStatic = false;
    private Instance staticValue;
    private boolean lazy;
    private Field template;
    private Field existing;

    private FieldBuilder(String name, @Nullable String code, ClassType declaringType, Type type) {
        this.name = name;
        this.code = code;
        this.declaringType = declaringType;
        this.type = type;
    }

    public FieldBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public FieldBuilder nullType(PrimitiveType nullType) {
        this.nullType = nullType;
        return this;
    }

    public FieldBuilder access(Access access) {
        this.access = access;
        return this;
    }

    public FieldBuilder unique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public FieldBuilder lazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public FieldBuilder column(Column column) {
        this.column = column;
        return this;
    }

    public FieldBuilder existing(Field existing) {
        this.existing = existing;
        return this;
    }

    public FieldBuilder asTitle(boolean asTitle) {
        this.asTitle = asTitle;
        return this;
    }

    public FieldBuilder defaultValue(Instance defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public FieldBuilder isChild(boolean isChild) {
        this.isChild = isChild;
        return this;
    }

    public FieldBuilder isStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return this;
    }

    public FieldBuilder staticValue(Instance staticValue) {
        this.staticValue = staticValue;
        return this;
    }

    public FieldBuilder template(Field template) {
        this.template = template;
        return this;
    }

    public Field build() {
        var effectiveDefaultValue = defaultValue != null ? defaultValue : new NullInstance(nullType());
        var effectiveStaticValue = staticValue != null ? staticValue : new NullInstance(nullType());
        if(existing == null) {
            var field = new Field(
                    tmpId,
                    name,
                    code,
                    declaringType,
                    type,
                    access,
                    unique,
                    asTitle,
                    effectiveDefaultValue,
                    isChild,
                    isStatic,
                    lazy,
                    effectiveStaticValue,
                    template,
                    column
            );
            return field;
        }
        else {
            existing.setTmpId(tmpId);
            existing.setName(name);
            existing.setCode(code);
            existing.setType(type);
            existing.setAccess(access);
            existing.setUnique(unique);
            existing.setLazy(lazy);
            existing.setAsTitle(asTitle);
            existing.setDefaultValue(effectiveDefaultValue);
            existing.setStaticValue(effectiveStaticValue);
            return existing;
        }
    }

    private PrimitiveType nullType() {
        return nullType != null ? nullType : StandardTypes.getNullType();
    }

}
