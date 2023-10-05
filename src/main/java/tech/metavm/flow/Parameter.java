package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.meta.Type;

import javax.annotation.Nullable;

@EntityType("参数")
public class Parameter extends Entity {
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("类型")
    private Type type;

    public Parameter(Long tmpId, String name, @Nullable String code, Type type) {
        setTmpId(tmpId);
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Type getType() {
        return type;
    }

    public Parameter copy() {
        return new Parameter(null, name, code, type);
    }

    public ParameterDTO toDTO() {
        try(var context = SerializeContext.enter()) {
            return new ParameterDTO(context.getTmpId(this), getId(), name, code, context.getRef(type));
        }
    }

}
