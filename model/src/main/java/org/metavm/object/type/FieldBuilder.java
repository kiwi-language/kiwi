package org.metavm.object.type;

import org.metavm.flow.Method;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Column;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class FieldBuilder {


    public static FieldBuilder newBuilder(String name, Klass declaringType, Type type) {
        return new FieldBuilder(name, declaringType, type);
    }

    private final String name;
    private final Klass declaringType;
    private final Type type;
    private Column column;
    private Long tmpId;
    private Access access = Access.PUBLIC;
    private boolean unique = false;
    private Value defaultValue;
    private boolean isChild;
    private boolean isStatic = false;
    private Value staticValue;
    private MetadataState state;
    private boolean lazy;
    private Field existing;
    private boolean readonly;
    private boolean isTransient;
    private boolean asTitle;
    private boolean isEnumConstant;
    private int tag = -1;
    private int ordinal = -1;
    private Integer sourceTag;
    private int since;
    private @Nullable Method initializer;

    private FieldBuilder(String name, Klass declaringType, Type type) {
        this.name = name;
        this.declaringType = declaringType;
        this.type = type;
    }

    public FieldBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
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

    public FieldBuilder tag(int tag) {
        this.tag = tag;
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

    public FieldBuilder defaultValue(Value defaultValue) {
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

    public FieldBuilder staticValue(Value staticValue) {
        this.staticValue = staticValue;
        return this;
    }

    public FieldBuilder readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public FieldBuilder isTransient(boolean isTransient) {
        this.isTransient = isTransient;
        return this;
    }

    public FieldBuilder sourceTag(Integer sourceTag) {
        this.sourceTag = sourceTag;
        return this;
    }

    public FieldBuilder since(int since) {
        this.since = since;
        return this;
    }

    public FieldBuilder initializer(@Nullable Method initializer) {
        this.initializer = initializer;
        return this;
    }

    public FieldBuilder isEnumConstant(boolean isEnumConstant) {
        this.isEnumConstant = isEnumConstant;
        return this;
    }

    public FieldBuilder ordinal(int ordinal) {
        this.ordinal = ordinal;
        return this;
    }

    public Field build() {
        Field field;
        if (existing == null) {
            if(defaultValue == null)
                defaultValue = Instances.nullInstance();
            if(staticValue == null)
                staticValue = Instances.nullInstance();
            if (state == null)
                state = defaultValue.isNotNull() ? MetadataState.INITIALIZING : MetadataState.READY;
            if(tag == -1)
                tag = declaringType.nextFieldTag();
            field = new Field(
                    tmpId,
                    name,
                    declaringType,
                    type,
                    access,
                    readonly,
                    isTransient,
                    unique,
                    defaultValue,
                    isChild,
                    isStatic,
                    lazy,
                    isEnumConstant,
                    ordinal,
                    column,
                    tag,
                    sourceTag,
                    since,
                    initializer,
                    state
            );
        } else {
            field = existing;
            existing.setTmpId(tmpId);
            existing.setName(name);
            existing.setType(type);
            existing.setAccess(access);
            existing.setUnique(unique);
            existing.setLazy(lazy);
            existing.setReadonly(readonly);
            existing.setTransient(isTransient);
            if(defaultValue != null)
                existing.setDefaultValue(defaultValue);
            if (state != null)
                existing.setState(state);
        }
        if(asTitle)
            declaringType.setTitleField(field);
        return field;
    }

}
