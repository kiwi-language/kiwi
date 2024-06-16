package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.ParsingContext;
import org.metavm.expression.TypeParsingContext;
import org.metavm.object.type.rest.dto.ConstraintDTO;
import org.metavm.object.type.rest.dto.ConstraintParam;
import org.metavm.util.NamingUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public abstract class Constraint extends Element implements  ClassMember, LocalKey {

    private final Klass declaringType;
    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String code;
    private final ConstraintKind kind;
    @Nullable
    private String message;

    public Constraint(ConstraintKind kind, @NotNull Klass declaringType,
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
                    serContext.getStringId(this),
                    kind.code(),
                    serContext.getStringId(declaringType),
                    name,
                    code,
                    message,
                    getParam()
            );
        }
    }

    @Override
    public Klass getDeclaringType() {
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
