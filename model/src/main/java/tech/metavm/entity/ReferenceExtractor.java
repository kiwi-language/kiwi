package tech.metavm.entity;

import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.util.StreamVisitor;

import java.io.InputStream;
import java.util.function.Consumer;

public class ReferenceExtractor extends StreamVisitor {

    private final long appId;
    private final Consumer<ReferencePO> add;
    private PhysicalId sourceId;
    private long fieldId;

    public ReferenceExtractor(InputStream in, long appId, Consumer<ReferencePO> add) {
        super(in);
        this.appId = appId;
        this.add = add;
    }

    @Override
    public void visitRecordBody(PhysicalId id) {
        if (sourceId != null)
            addReference(id);
        var oldSourceId = sourceId;
        var oldFieldId = fieldId;
        sourceId = id;
        fieldId = -1L;
        super.visitRecordBody(id);
        sourceId = oldSourceId;
        fieldId = oldFieldId;
    }

    private void addReference(PhysicalId targetId) {
        add.accept(new ReferencePO(this.appId,
                sourceId.getId(), sourceId.getTypeTag().code(),
                sourceId.getTypeId(),
                targetId.getId(), targetId.getTypeTag().code(),
                targetId.getTypeId(),
                fieldId,
                ReferenceKind.STRONG.code()));
    }

    @Override
    public void visitField() {
        fieldId = readLong();
        visit();
    }

    @Override
    public void visitReference() {
        addReference(readId());
    }
}
