package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("属性")
public abstract class Property extends Entity {

    public static final IndexDef<Property> INDEX_TYPE = new IndexDef<>(Property.class, false,"type");

    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("类型")
    private Type type;
    @EntityField("所属类型")
    protected final ClassType declaringType;

    public Property(Long tmpId, String name, @Nullable String code, Type type, ClassType declaringType) {
        super(tmpId);
        this.name = name;
        this.code = code;
        this.type = type;
        this.declaringType = declaringType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public String getCodeRequired() {
        return NncUtils.requireNonNull(code, "code is set for type " + getName());
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }
}
