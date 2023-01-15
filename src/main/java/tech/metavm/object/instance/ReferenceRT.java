package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.Field;
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
            classInstance.set(NncUtils.requireNonNull(field), InstanceUtils.nullInstance());
        } else if (source instanceof ArrayInstance arrayInstance) {
            arrayInstance.remove(target);
        } else {
            throw new InternalException("Unexpected source: " + source);
        }
    }

    public ReferencePO toPO(long tenantId) {
        return new ReferencePO(
                tenantId,
                source().getId(),
                target.getId(),
                field != null ? field.getId() : -1L,
                getKind().code()
        );
    }

}
