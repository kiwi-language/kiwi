package tech.metavm.entity;

import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.util.StreamVisitor;

import java.io.InputStream;
import java.util.function.Consumer;

public class ReferenceExtractor extends StreamVisitor {

    private final long appId;
    private final Consumer<ReferencePO> add;
    private long sourceId = -1L;
    private long fieldId = -1L;

    public ReferenceExtractor(InputStream in, long appId, Consumer<ReferencePO> add) {
        super(in);
        this.appId = appId;
        this.add = add;
    }

    @Override
    public void visitRecordBody(long id) {
        if (sourceId != -1L)
            addReference(id);
        var oldSourceId = sourceId;
        var oldFieldId = fieldId;
        sourceId = id;
        fieldId = -1L;
        super.visitRecordBody(id);
        sourceId = oldSourceId;
        fieldId = oldFieldId;
    }

    private void addReference(long appId) {
        add.accept(new ReferencePO(this.appId, sourceId, appId, fieldId,
                ReferenceKind.STRONG.code()));
    }

    @Override
    public void visitField() {
        fieldId = readLong();
        visit();
    }

    @Override
    public void visitReference() {
        addReference(readLong());
    }
}
