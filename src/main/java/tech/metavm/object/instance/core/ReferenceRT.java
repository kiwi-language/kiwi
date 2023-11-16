package tech.metavm.object.instance.core;

import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.type.Field;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record ReferenceRT(Instance source, Instance target, @Nullable Field field) {

    public ReferenceRT(Instance source, Instance target, @Nullable Field field) {
        this.source = source;
        this.target = target;
        this.field = field;
        source.addOutgoingReference(this);
        target.addIncomingReference(this);
    }

    public ReferenceKind getKind() {
        return field != null && field.getType().isUnionNullable() ? ReferenceKind.WEAK : ReferenceKind.STRONG;
    }

    public boolean isStrong() {
        return getKind() == ReferenceKind.STRONG;
    }

    public void clear() {
        source.removeOutgoingReference(this);
        target.removeIncomingReference(this);
    }

    public void remove() {
        if (source instanceof ClassInstance classInstance) {
            classInstance.setField(NncUtils.requireNonNull(field), InstanceUtils.nullInstance());
        } else if (source instanceof ArrayInstance arrayInstance) {
            arrayInstance.remove(target);
        } else {
            throw new InternalException("Unexpected source: " + source);
        }
    }

}
