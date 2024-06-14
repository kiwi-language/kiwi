package org.metavm.object.instance.core;

import org.metavm.object.instance.ReferenceKind;
import org.metavm.object.type.Field;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record ReferenceRT(DurableInstance source, DurableInstance target, @Nullable Field field) {

    public ReferenceRT(DurableInstance source, DurableInstance target, @Nullable Field field) {
        this.source = source;
        this.target = target;
        this.field = field;
        source.addOutgoingReference(this);
    }

    public ReferenceKind getKind() {
        return field != null && field.getType().isNullable() ? ReferenceKind.WEAK : ReferenceKind.STRONG;
    }

    public boolean isStrong() {
        return getKind() == ReferenceKind.STRONG;
    }

    public void clear() {
        source.removeOutgoingReference(this);
    }

    public void remove() {
        if (source instanceof ClassInstance classInstance) {
            classInstance.setField(NncUtils.requireNonNull(field), Instances.nullInstance());
        } else if (source instanceof ArrayInstance arrayInstance) {
            arrayInstance.removeElement(target);
        } else {
            throw new InternalException("Unexpected source: " + source);
        }
    }

}
