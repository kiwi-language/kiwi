package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Column;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class FieldBuilder {

    public static FieldBuilder newBuilder(String name, Klass declaringType, Type type) {
        return new FieldBuilder(name, declaringType, type);
    }

    public static FieldBuilder newBuilder(String name, Klass declaringType, int typeIndex) {
        return new FieldBuilder(name, declaringType, typeIndex);
    }

    private final String name;
    private final Klass declaringType;
    private final @Nullable Type type;
    private final int typeIndex;
    private Column column;
    private Id id;
    private Access access = Access.PUBLIC;
    private Value defaultValue;
    private boolean isStatic = false;
    private Value staticValue;
    private MetadataState state;
    private boolean lazy;
    private boolean readonly;
    private boolean isTransient;
    private boolean asTitle;
    private boolean isEnumConstant;
    private int tag = -1;
    private int ordinal = -1;
    private Integer sourceTag;
    private int since;
    private @Nullable Method initializer;

    private FieldBuilder(String name, Klass declaringType, @NotNull Type type) {
        this.name = name;
        this.declaringType = declaringType;
        this.type = type;
        this.typeIndex = -1;
    }

    private FieldBuilder(String name, Klass declaringType, int typeIndex) {
        this.name = name;
        this.declaringType = declaringType;
        this.type = null;
        assert typeIndex >= 0;
        this.typeIndex = typeIndex;
    }

    public FieldBuilder tmpId(Long tmpId) {
        this.id = TmpId.of(tmpId);
        return this;
    }

    public FieldBuilder id(Id id) {
        this.id = id;
        return this;
    }

    public FieldBuilder access(Access access) {
        this.access = access;
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

    public FieldBuilder defaultValue(Value defaultValue) {
        this.defaultValue = defaultValue;
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
        if(defaultValue == null)
            defaultValue = Instances.nullInstance();
        if(staticValue == null)
            staticValue = Instances.nullInstance();
        if (state == null)
            state = defaultValue.isNotNull() ? MetadataState.INITIALIZING : MetadataState.READY;
        if(tag == -1)
            tag = declaringType.nextFieldTag();
        if (id == null)
            id = declaringType.getRoot().nextChildId();
        int typeIndex = type != null ? declaringType.getConstantPool().addValue(type) : this.typeIndex;
        field = new Field(
                id,
                name,
                declaringType,
                typeIndex,
                access,
                readonly,
                isTransient,
                defaultValue,
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
        if(asTitle)
            declaringType.setTitleField(field);
        return field;
    }

}
