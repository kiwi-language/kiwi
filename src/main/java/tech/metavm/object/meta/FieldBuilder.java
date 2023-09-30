package tech.metavm.object.meta;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.NullInstance;

import javax.annotation.Nullable;

public class FieldBuilder {

    public static FieldBuilder newBuilder(String name, @Nullable String code, ClassType declaringType, Type type) {
        return new FieldBuilder(name, code, declaringType, type);
    }

    private final String name;
    private final @Nullable String code;
    private final ClassType declaringType;
    private final Type type;
    private Long tmpId;
    private Access access = Access.GLOBAL;
    private boolean unique = false;
    private boolean asTitle = false;
    private PrimitiveType nullType;
    private Instance defaultValue;
    private boolean isChild;
    private boolean isStatic = false;
    private Instance staticValue;
    private Field template;

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
        var field = new Field(
                name,
                code,
                declaringType,
                type, access,
                unique,
                asTitle,
                defaultValue != null ? defaultValue : new NullInstance(nullType()),
                isChild,
                isStatic,
                staticValue != null ? staticValue : new NullInstance(nullType()),
                template
        );
        field.setTmpId(tmpId);
        return field;
    }

    private PrimitiveType nullType() {
        return nullType != null ? nullType : StandardTypes.getNullType();
    }

}
