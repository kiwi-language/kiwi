package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.api.ValueObject;
import org.metavm.common.ErrorCode;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.ParentRefDTO;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.InstanceParentRef;
import org.metavm.util.BusinessException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class ParentRef extends Element implements ValueObject {

    public static ParentRef create(ParentRefDTO parentRefDTO, ParsingContext parsingContext, IEntityContext entityContext, @Nullable Type childType) {
        var master = ValueFactory.create(parentRefDTO.parent(), parsingContext);
        var fieldRef = NncUtils.get(parentRefDTO.fieldRef(), ref -> FieldRef.create(ref, entityContext));
        var masterRef = new ParentRef(master, fieldRef);
        if (childType != null) {
            masterRef.ensureChildAssignable(childType);
        }
        return masterRef;
    }

    private final Value parent;
    private final @Nullable FieldRef fieldRef;

    public ParentRef(Value parent, @Nullable FieldRef parentField) {
        check(parent, parentField);
        this.parent = parent;
        this.fieldRef = parentField;
    }

    public InstanceParentRef evaluate(EvaluationContext context) {
        return new InstanceParentRef((Reference) parent.evaluate(context), NncUtils.get(fieldRef, FieldRef::resolve));
    }

    public ParentRefDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new ParentRefDTO(
                    parent.toDTO(),
                    NncUtils.get(fieldRef, f -> f.toDTO(serContext))
            );
        }
    }

    public void ensureChildAssignable(Type childType) {
        if (fieldRef != null) {
            var field = fieldRef.resolve();
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
        return NncUtils.get(fieldRef, FieldRef::resolve);
    }

    public boolean isEmpty() {
        return parent == null;
    }

    private static void check(Value master, FieldRef masterField) {
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
        if (fieldRef != null)
            text += "." + fieldRef.resolve().getName();
        return text;
    }

}
