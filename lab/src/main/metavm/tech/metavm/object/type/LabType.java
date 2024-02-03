package tech.metavm.object.type;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType("类型")
public class LabType {

    @EntityField(value = "名称", asTitle = true)
    protected String name;
    @EntityField(value = "编号")
    private @Nullable String code;
    @EntityField("是否匿名")
    protected final boolean anonymous;
    @EntityField("是否临时")
    protected final boolean ephemeral;
    @EntityField("类别")
    protected final LabTypeCategory category;
    @EntityField("错误")
    private boolean error = false;

    public LabType(String name, @Nullable String code, boolean anonymous, boolean ephemeral, LabTypeCategory category) {
        this.name = name;
        this.code = code;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public LabTypeCategory getCategory() {
        return category;
    }

    public boolean isError() {
        return error;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
