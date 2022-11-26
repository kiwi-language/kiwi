package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.NncUtils;

@EntityType("约束")
public abstract class ConstraintRT<T> extends Entity {

    @EntityField("所属类型")
    private final Type declaringType;
    @EntityField("类别")
    private final ConstraintKind kind;
    @EntityField("错误提示")
    private String message;

    public ConstraintRT(ConstraintKind kind, Type declaringType, String message) {
        this.declaringType = declaringType;
        this.kind = kind;
        this.message = message;
    }

    //    public ConstraintRT(ConstraintPO po/*, EntityContext context*/) {
//        super(po.getId());
//        kind = ConstraintKind.getByCode(po.getKind());
//        this.declaringType = context.getType(po.getDeclaringTypeId());
//        this.message = po.getMessage();
//    }
//
//    public ConstraintRT(ConstraintKind kind, Type declaringType, String message) {
//        super(declaringType.getContext());
//        this.kind = kind;
//        this.declaringType = declaringType;
//        setMessage(message);
//        declaringType.addConstraint(this);
//    }

    public String getMessage() {
        return message;
    }

    public abstract String getDefaultMessage();

    public void setMessage(String message) {
        this.message = message;
    }

    protected abstract T getParam(boolean forPersistence);

    protected abstract void setParam(T param);

    public ConstraintDTO toDTO() {
        return new ConstraintDTO(
                getId(),
                kind.code(),
                declaringType.getId(),
                message,
                getParam(false)
        );
    }

    public abstract String getDesc();

    @JsonIgnore
    public Type getDeclaringType() {
        return declaringType;
    }

    public ConstraintPO toPO() {
        return new ConstraintPO(
                getId(),
                declaringType.getId(),
                kind.code(),
                message,
                NncUtils.toJSONString(getParam(true))
        );
    }

    public void update(ConstraintDTO constraintDTO) {
        setMessage(constraintDTO.message());
        setParam(constraintDTO.getParam());
    }

}
