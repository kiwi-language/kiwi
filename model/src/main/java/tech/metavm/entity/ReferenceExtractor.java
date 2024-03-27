package tech.metavm.entity;

import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.util.Constants;
import tech.metavm.util.NncUtils;
import tech.metavm.util.StreamVisitor;

import java.io.InputStream;
import java.util.function.Consumer;

public class ReferenceExtractor extends StreamVisitor {

    private final long appId;
    private final Consumer<ReferencePO> add;
    private Id sourceId;
    private Id fieldId;

    public ReferenceExtractor(InputStream in, long appId, Consumer<ReferencePO> add) {
        super(in);
        this.appId = appId;
        this.add = add;
    }

    @Override
    public void visitRecordBody(Id id) {
        if (sourceId != null)
            addReference(id);
        var oldSourceId = sourceId;
        var oldFieldId = fieldId;
        sourceId = id;
        fieldId = null;
        super.visitRecordBody(id);
        sourceId = oldSourceId;
        fieldId = oldFieldId;
    }

    private void addReference(Id targetId) {
        if(sourceId.getPhysicalId() != targetId.getPhysicalId()) {
            add.accept(new ReferencePO(this.appId,
                    sourceId.getPhysicalId(),
                    targetId.toBytes(),
                    NncUtils.getOrElse(fieldId, Id::toBytes, Constants.EMPTY_BYTES),
                    ReferenceKind.STRONG.code()));
        }
    }

    @Override
    public void visitField() {
        fieldId = readId();
        visit();
    }

    @Override
    public void visitReference() {
        addReference(readId());
    }
}
