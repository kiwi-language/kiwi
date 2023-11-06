package tech.metavm.flow;

import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.ParentRefDTO;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("主对象引用")
public class ParentRef extends Entity {

    public static ParentRef create(ParentRefDTO masterRefDTO, ParsingContext parsingContext, @Nullable Type childType) {
        var master = ValueFactory.create(masterRefDTO.parent(), parsingContext);
        var field = NncUtils.get(masterRefDTO.fieldRef(),
                Objects.requireNonNull(parsingContext.getEntityContext())::getField);
        var masterRef = new ParentRef(master, field);
        if (childType != null) {
            masterRef.ensureChildAssignable(childType);
        }
        return masterRef;
    }

    @ChildEntity("主对象")
    private final Value parent;
    @EntityField("主对象字段")
    private final @Nullable Field field;

    public ParentRef(Value master, @Nullable Field masterField) {
        check(master, masterField);
        this.parent = master;
        this.field = masterField;
    }

    public InstanceParentRef evaluate(EvaluationContext context) {
        return new InstanceParentRef(parent.evaluate(context), field);
    }

    public ParentRefDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new ParentRefDTO(
                    parent.toDTO(false),
                    NncUtils.get(field, serContext::getRef)
            );
        }
    }

    public void ensureChildAssignable(Type childType) {
        if (field != null) {
            if (!field.getType().isAssignableFrom(childType)) {
                throw new BusinessException(ErrorCode.INVALID_MASTER,
                        parent.getType().getName() + "." + field.getName());
            }
        } else {
            var arrayType = (ArrayType) parent.getType();
            if (!arrayType.getElementType().isAssignableFrom(childType)) {
                throw new BusinessException(ErrorCode.INVALID_MASTER, parent.getType().getName());
            }
        }
    }

    public Value getParent() {
        return parent;
    }

    @Nullable
    public Field getField() {
        return field;
    }

    private static void check(Value master, Field masterField) {
        if (master != null) {
            if (master.getType() instanceof ClassType) {
                if (masterField == null) {
                    throw new BusinessException(ErrorCode.MASTER_FIELD_REQUIRED);
                }
            } else if (master.getType() instanceof ArrayType) {
                if (masterField != null) {
                    throw new BusinessException(ErrorCode.MASTER_FIELD_SHOULD_BE_NULL);
                }
            } else {
                throw new BusinessException(ErrorCode.INVALID_MASTER, master.getType().getName());
            }
        }
    }

}
