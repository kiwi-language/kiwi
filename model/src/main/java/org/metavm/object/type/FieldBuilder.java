package org.metavm.object.type;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.NullInstance;
import org.metavm.util.Column;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public class FieldBuilder {

    public static FieldBuilder newBuilder(String name, @Nullable String code, Klass declaringType, Type type) {
        return new FieldBuilder(name, code, declaringType, type);
    }

    private final String name;
    @Nullable
    private final String code;
    private final Klass declaringType;
    private final Type type;
    private Column column;
    private Long tmpId;
    private Access access = Access.PUBLIC;
    private boolean unique = false;
    private PrimitiveType nullType;
    private Instance defaultValue;
    private boolean isChild;
    private boolean isStatic = false;
    private Instance staticValue;
    private MetadataState state;
    private boolean lazy;
    private Field template;
    private Field existing;
    private boolean readonly;
    private boolean asTitle;

    private FieldBuilder(String name, @Nullable String code, Klass declaringType, Type type) {
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

    public FieldBuilder state(MetadataState state) {
        this.state = state;
        return this;
    }

    public FieldBuilder lazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public FieldBuilder asTitle() {
        this.asTitle = true;
        return this;
    }

    public FieldBuilder asTitle(boolean asTitle) {
        this.asTitle = asTitle;
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

    public FieldBuilder readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    private PrimitiveType getNullType() {
        return NncUtils.orElse(nullType, Types::getNullType);
    }

    public Field build() {
        Field field;
        if (existing == null) {
            if(defaultValue == null)
                defaultValue = new NullInstance(getNullType());
            if(staticValue == null)
                staticValue = new NullInstance(getNullType());
            if (state == null)
                state = defaultValue.isNotNull() ? MetadataState.INITIALIZING : MetadataState.READY;
            field = new Field(
                    tmpId,
                    name,
                    code,
                    declaringType,
                    type,
                    access,
                    readonly,
                    unique,
                    defaultValue,
                    isChild,
                    isStatic,
                    lazy,
                    staticValue,
                    column,
                    state
            );
        } else {
            field = existing;
            existing.setTmpId(tmpId);
            existing.setName(name);
            existing.setCode(code);
            existing.setType(type);
            existing.setAccess(access);
            existing.setUnique(unique);
            existing.setLazy(lazy);
            existing.setReadonly(readonly);
            if(defaultValue != null)
                existing.setDefaultValue(defaultValue);
            if(staticValue != null)
                existing.setStaticValue(staticValue);
            if (state != null)
                existing.setState(state);
        }
        if(asTitle)
            declaringType.setTitleField(field);
        return field;
    }

    private PrimitiveType nullType() {
        return nullType != null ? nullType : Types.getNullType();
    }

}
