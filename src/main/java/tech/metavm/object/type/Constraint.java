package tech.metavm.object.type;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.rest.dto.ConstraintDTO;

import javax.annotation.Nullable;

@EntityType("约束")
public abstract class Constraint extends ClassMember {

    @EntityField("类别")
    private final ConstraintKind kind;
    @EntityField("错误提示")
    @Nullable
    private String message;

    public Constraint(ConstraintKind kind, ClassType declaringType, @Nullable String message) {
        super(null, declaringType);
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

    protected abstract Object getParam(boolean forPersistence);

    public ConstraintDTO toDTO() {
        try(var context = SerializeContext.enter()) {
            return new ConstraintDTO(
                    context.getTmpId(this),
                    getId(),
                    kind.code(),
                    getDeclaringType().getIdRequired(),
                    message,
                    getParam(false)
            );
        }
    }

    public abstract String getDesc();

    public void update(ConstraintDTO constraintDTO) {
        setMessage(constraintDTO.message());
    }

}
