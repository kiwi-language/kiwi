package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.ParentRefDTO;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("父引用")
public class ParentRef extends Element {

    public static ParentRef create(ParentRefDTO parentRefDTO, ParsingContext parsingContext, IEntityContext entityContext, @Nullable Type childType) {
        var master = ValueFactory.create(parentRefDTO.parent(), parsingContext);
        var field = NncUtils.get(parentRefDTO.fieldId(), id -> entityContext.getField(Id.parse(id)));
        var masterRef = new ParentRef(master, field);
        if (childType != null) {
            masterRef.ensureChildAssignable(childType);
        }
        return masterRef;
    }

    @ChildEntity("父对象")
    private final Value parent;
    @EntityField("父字段")
    @Nullable
    private final Field field;

    public ParentRef(Value parent, @Nullable Field parentField) {
        check(parent, parentField);
        this.parent = addChild(parent, "parent");
        this.field = parentField;
    }

    public InstanceParentRef evaluate(EvaluationContext context) {
        return new InstanceParentRef((DurableInstance) parent.evaluate(context), field);
    }

    public ParentRefDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new ParentRefDTO(
                    parent.toDTO(),
                    NncUtils.get(field, serContext::getId)
            );
        }
    }

    public void ensureChildAssignable(Type childType) {
        if (field != null) {
            if (!field.getType().isAssignableFrom(childType, null)) {
                throw new BusinessException(ErrorCode.INVALID_MASTER,
                        parent.getType().getName() + "." + field.getName());
            }
        } else {
            var arrayType = (ArrayType) parent.getType();
            if (!arrayType.getElementType().isAssignableFrom(childType, null)) {
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

    public boolean isEmpty() {
        return parent == null;
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParentRef(this);
    }

    public String getText() {
        String text = "as child of " + parent.getText();
        if (field != null)
            text += "." + field.getName();
        return text;
    }

}
