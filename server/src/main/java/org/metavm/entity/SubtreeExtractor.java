package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.InstanceOutput;
import org.metavm.util.StreamCopier;
import org.metavm.util.StreamVisitor;
import org.metavm.util.WireTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

public class SubtreeExtractor extends StreamVisitor {

    private static final Logger logger = LoggerFactory.getLogger(SubtreeExtractor.class);

    private Id parentId;
    private long parentFieldTag = -1L;
    private final Consumer<Subtree> add;

    public SubtreeExtractor(InputStream in, Consumer<Subtree> add) {
        super(in);
        this.add = add;
    }

    @Override
    public void visitRecordBody(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.RECORD);
        output.writeLong(nodeId);
        typeOrTypeKey.write(output);
        var oldParentId = parentId;
        var oldParentFieldTag = parentFieldTag;
        var id = PhysicalId.of(treeId, nodeId, typeOrTypeKey);
        parentId = id;
        parentFieldTag = -1;
        new StreamCopier(getInput(), output) {
            @Override
            public void visitField() {
                writeLong(parentFieldTag = readLong());
                visit();
            }

            @Override
            public void visitRecord(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
                var typeKey = readTypeKey();
                write(WireTypes.REFERENCE);
                writeId(PhysicalId.of(treeId, nodeId, typeKey));
                SubtreeExtractor.this.visitRecordBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeKey);
            }
        }.visitRecordBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeOrTypeKey);
        parentId = oldParentId;
        parentFieldTag = oldParentFieldTag;
        var oldId = oldTreeId != -1L ? PhysicalId.of(oldTreeId, oldNodeId, typeOrTypeKey) : null;
        add.accept(new Subtree(
                id,
                parentId,
                parentFieldTag,
                oldId,
                useOldId,
                bout.toByteArray()
        ));
    }

}
