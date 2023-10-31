package tech.metavm.flow;

import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.MasterRefDTO;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("主对象引用")
public class MasterRef extends Entity {

    public static MasterRef create(MasterRefDTO masterRefDTO, ParsingContext parsingContext, @Nullable Type childType) {
        var master = ValueFactory.create(masterRefDTO.master(), parsingContext);
        var field = NncUtils.get(masterRefDTO.fieldRef(),
                Objects.requireNonNull(parsingContext.getEntityContext())::getField);
        var masterRef = new MasterRef(master, field);
        if (childType != null) {
            masterRef.ensureChildAssignable(childType);
        }
        return masterRef;
    }

    @ChildEntity("值对象")
    private final Value master;
    @EntityField("主对象字段")
    private final @Nullable Field field;

    public MasterRef(Value master, @Nullable Field masterField) {
        check(master, masterField);
        this.master = master;
        this.field = masterField;
    }

    public void setChild(Instance child, MetaFrame frame) {
        var masterInstance = master.evaluate(frame);
        if (masterInstance instanceof ClassInstance classInstance) {
            classInstance.setField(NncUtils.requireNonNull(field), child);
        } else if (masterInstance instanceof ArrayInstance arrayInstance) {
            NncUtils.requireTrue(arrayInstance.isChildArray());
            arrayInstance.add(child);
        } else {
            throw new InternalException("Invalid master instance: " + masterInstance);
        }
    }

    public MasterRefDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new MasterRefDTO(
                    master.toDTO(false),
                    NncUtils.get(field, serContext::getRef)
            );
        }
    }

    public void ensureChildAssignable(Type childType) {
        if (field != null) {
            if (!field.getType().isAssignableFrom(childType)) {
                throw new BusinessException(ErrorCode.INVALID_MASTER,
                        master.getType().getName() + "." + field.getName());
            } else {
                var arrayType = (ArrayType) master.getType();
                if (!arrayType.getElementType().isAssignableFrom(childType)) {
                    throw new BusinessException(ErrorCode.INVALID_MASTER, master.getType().getName());
                }
            }
        }
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
