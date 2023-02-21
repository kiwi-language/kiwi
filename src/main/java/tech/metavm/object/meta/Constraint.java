package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("约束")
public abstract class Constraint<T> extends Entity {

    @EntityField("所属类型")
    private final ClassType declaringType;
    @EntityField("类别")
    private final ConstraintKind kind;
    @EntityField("错误提示")
    @Nullable
    private String message;

    public Constraint(ConstraintKind kind, ClassType declaringType, @Nullable String message) {
        this.declaringType = declaringType;
        this.kind = kind;
        this.message = message;
        declaringType.addConstraint(this);
    }

    public @Nullable String getMessage() {
        return message;
    }

    public abstract String getDefaultMessage();

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    protected abstract T getParam(boolean forPersistence);

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
    public ClassType getDeclaringType() {
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
    }

    @Override
    public List<Object> beforeRemove() {
        declaringType.removeConstraint(this);
        return List.of();
    }
}
