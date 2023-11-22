package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("属性")
public abstract class Property extends ClassMember {

    public static final IndexDef<Property> INDEX_TYPE = new IndexDef<>(Property.class, false,"type");

    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("类型")
    private Type type;
    @EntityField("状态")
    private MetadataState state;

    public Property(Long tmpId, String name, @Nullable String code, Type type, ClassType declaringType, MetadataState state) {
        super(tmpId, declaringType);
        this.name = name;
        this.code = code;
        this.type = type;
        this.state = state;
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

    public MetadataState getState() {
        return state;
    }

    public boolean isReady() {
        return state == MetadataState.READY;
    }

    public void setState(MetadataState state) {
        this.state = state;
    }
}
