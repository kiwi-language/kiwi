package org.metavm.entity;

import org.metavm.object.instance.ReferenceKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.util.StreamVisitor;

import java.io.InputStream;
import java.util.function.Consumer;

public class ReferenceExtractor extends StreamVisitor {

    private final long appId;
    private final Consumer<ReferencePO> add;

    public ReferenceExtractor(InputStream in, long appId, Consumer<ReferencePO> add) {
        super(in);
        this.appId = appId;
        this.add = add;
    }

    private void addReference(Id targetId) {
        var treeId = getTreeId();
        if(treeId != targetId.getTreeId()) {
            add.accept(new ReferencePO(this.appId,
                    treeId,
                    targetId.toBytes(),
                    ReferenceKind.STRONG.code()));
        }
    }

    @Override
    public void visitReference() {
        addReference(readId());
    }
}
