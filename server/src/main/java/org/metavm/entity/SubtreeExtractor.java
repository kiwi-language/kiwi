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
    private final Consumer<Subtree> add;

    public SubtreeExtractor(InputStream in, Consumer<Subtree> add) {
        super(in);
        this.add = add;
    }

    @Override
    public void visitInstanceBody(long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.INSTANCE);
        output.writeLong(nodeId);
        typeOrTypeKey.write(output);
        var oldParentId = parentId;
        var id = PhysicalId.of(treeId, nodeId);
        parentId = id;
        new StreamCopier(getInput(), output) {
            @Override
            public void visitField() {
                writeLong(readLong());
                visitValue();
            }

            @Override
            public void visitInstance(long treeId, long nodeId) {
                var typeKey = readTypeKey();
                write(WireTypes.REFERENCE);
                writeId(PhysicalId.of(treeId, nodeId));
                SubtreeExtractor.this.visitInstanceBody(treeId, nodeId, typeKey);
            }
        }.visitInstanceBody(treeId, nodeId, typeOrTypeKey);
        parentId = oldParentId;
        add.accept(new Subtree(
                id,
                parentId,
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
                bout.toByteArray(),
                tag
        ));
    }

}
