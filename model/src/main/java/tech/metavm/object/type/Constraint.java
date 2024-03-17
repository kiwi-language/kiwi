package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.ParsingContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.object.type.rest.dto.ConstraintDTO;
import tech.metavm.object.type.rest.dto.ConstraintParam;
import tech.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("约束")
public abstract class Constraint extends Element implements  ClassMember, LocalKey {

    @EntityField("所属类型")
    private final ClassType declaringType;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField(value = "编号")
    @Nullable
    private String code;
    @EntityField("类别")
    private final ConstraintKind kind;
    @EntityField("错误提示")
    @Nullable
    private String message;

    public Constraint(ConstraintKind kind, @NotNull ClassType declaringType,
                      String name, @Nullable String code, @Nullable String message) {
        super(null);
        this.declaringType = declaringType;
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

    protected abstract ConstraintParam getParam();

    public abstract void setParam(Object param, IEntityContext context);

    protected ParsingContext getParsingContext(IEntityContext context) {
        return TypeParsingContext.create(getDeclaringType(), context);
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
        try (var serContext = SerializeContext.enter()) {
            return new ConstraintDTO(
                    serContext.getId(this),
                    kind.code(),
                    serContext.getId(declaringType),
                    name,
                    code,
                    message,
                    getParam()
            );
        }
    }

    @Override
    public ClassType getDeclaringType() {
        return declaringType;
    }

    public abstract String getDesc();

    public void update(ConstraintDTO constraintDTO, IEntityContext context) {
        setName(constraintDTO.name());
        setCode(constraintDTO.code());
        setMessage(constraintDTO.message());
        if(constraintDTO.param() != null)
            setParam(constraintDTO.getParam(), context);
    }

    @Override
    public boolean isValidLocalKey() {
        return code != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(code);
    }
}
