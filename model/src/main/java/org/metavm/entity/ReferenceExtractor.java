package org.metavm.entity;

import org.metavm.object.instance.ReferenceKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.ReferencePO;
import org.metavm.util.StreamVisitor;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ReferenceExtractor extends StreamVisitor {

    private final long appId;
    private final Consumer<ReferencePO> add;
    private Set<Id> forwarded;

    public ReferenceExtractor(InputStream in, long appId, Consumer<ReferencePO> add) {
        super(in);
        this.appId = appId;
        this.add = add;
    }

    @Override
    public void visitForwardingPointer() {
        if(forwarded == null)
            forwarded = new HashSet<>();
        forwarded.add(readId());
        readId();
    }

    private void addReference(Id targetId) {
        var treeId = getTreeId();
        if(treeId != targetId.getTreeId() || forwarded != null && forwarded.contains(targetId)) {
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
