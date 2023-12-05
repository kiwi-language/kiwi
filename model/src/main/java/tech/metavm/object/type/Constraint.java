package tech.metavm.object.type;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.object.type.rest.dto.ConstraintDTO;
import tech.metavm.object.type.rest.dto.ConstraintParam;
import tech.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("约束")
public abstract class Constraint extends ClassMember {

    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField(value = "编号")
    private @Nullable String code;
    @EntityField("类别")
    private final ConstraintKind kind;
    @EntityField("错误提示")
    @Nullable
    private String message;

    public Constraint(ConstraintKind kind, ClassType declaringType,
                      String name, @Nullable String code, @Nullable String message) {
        super(null, declaringType);
        this.name =  NamingUtils.ensureValidName(name);
        this.code = NamingUtils.ensureValidCode(code);
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

    protected abstract ConstraintParam getParam(boolean forPersistence);

    public abstract void setParam(Object param, IEntityContext context);

    protected ParsingContext getParsingContext(IEntityContext context) {
        return new TypeParsingContext(getDeclaringType(), Objects.requireNonNull(context.getInstanceContext()));
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = NamingUtils.ensureValidName(name);
    }

    public void setCode(@Nullable String code) {
        this.code = NamingUtils.ensureValidCode(code);
    }

    public ConstraintDTO toDTO() {
        try (var context = SerializeContext.enter()) {
            return new ConstraintDTO(
                    context.getTmpId(this),
                    getId(),
                    kind.code(),
                    getDeclaringType().getIdRequired(),
                    name,
                    code,
                    message,
                    getParam(false)
            );
        }
    }

    public abstract String getDesc();

    public void update(ConstraintDTO constraintDTO, IEntityContext context) {
        setName(constraintDTO.name());
        setCode(constraintDTO.code());
        setMessage(constraintDTO.message());
        if(constraintDTO.param() != null)
            setParam(constraintDTO.getParam(), context);
    }

}
