package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.InstanceOutput;
import org.metavm.util.StreamCopier;
import org.metavm.util.StreamVisitor;
import org.metavm.util.WireTypes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

@Slf4j
public class SubtreeExtractor extends StreamVisitor {

    private Id parentId;
    private long parentFieldTag = -1L;
    private final Consumer<Subtree> add;

    public SubtreeExtractor(InputStream in, Consumer<Subtree> add) {
        super(in);
        this.add = add;
    }

    @Override
    public void visitInstanceBody(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.INSTANCE);
        output.writeLong(nodeId);
        typeOrTypeKey.write(output);
        var oldParentId = parentId;
        var oldParentFieldTag = parentFieldTag;
        var id = PhysicalId.of(treeId, nodeId);
        parentId = id;
        parentFieldTag = -1;
        new StreamCopier(getInput(), output) {
            @Override
            public void visitField() {
                writeLong(parentFieldTag = readLong());
                visitValue();
            }

            @Override
            public void visitInstance(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
                var typeKey = readTypeKey();
                write(WireTypes.REFERENCE);
                writeId(PhysicalId.of(treeId, nodeId));
                SubtreeExtractor.this.visitInstanceBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeKey);
            }
        }.visitInstanceBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeOrTypeKey);
        parentId = oldParentId;
        parentFieldTag = oldParentFieldTag;
        var oldId = oldTreeId != -1L ? PhysicalId.of(oldTreeId, oldNodeId) : null;
        add.accept(new Subtree(
                id,
                parentId,
                parentFieldTag,
                oldId,
                useOldId,
                bout.toByteArray(),
                -1
        ));
    }

    @Override
    public void visitEntityBody(int tag, Id id) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(tag);
        output.writeId(id);
        var oldParentId = parentId;
        parentId = id;
        new StreamCopier(getInput(), output) {

            @Override
            public void visitEntity() {
                var tag = read();
                var id = readId();
                write(tag);
                writeId(id);
                SubtreeExtractor.this.visitEntityBody(tag, id);
            }

        }.visitEntityBody(tag, id);
        parentId = oldParentId;
        add.accept(new Subtree(
                id,
                parentId,
                parentFieldTag,
                null,
                false,
                bout.toByteArray(),
                tag
        ));
    }

}
