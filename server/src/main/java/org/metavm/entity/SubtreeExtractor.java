package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.util.InstanceOutput;
import org.metavm.util.StreamCopier;
import org.metavm.util.StreamVisitor;
import org.metavm.util.WireTypes;
import org.metavm.wire.WireAdapter;

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
    public void visitInstanceBody(long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey, int refcount) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.INSTANCE);
        output.writeLong(nodeId);
        typeOrTypeKey.write(output);
        output.writeInt(refcount);
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
                var rc = readInt();
                write(WireTypes.REFERENCE);
                writeId(PhysicalId.of(treeId, nodeId));
                SubtreeExtractor.this.visitInstanceBody(treeId, nodeId, typeKey, rc);
            }
        }.visitInstanceBody(treeId, nodeId, typeOrTypeKey, refcount);
        parentId = oldParentId;
        add.accept(new Subtree(
                id,
                parentId,
                bout.toByteArray(),
                -1
        ));
    }

    @Override
    public void visitEntity0(WireAdapter<?> adapter) {
        visitEntity1(adapter);
    }

    private Subtree visitEntity1(WireAdapter<?> adapter) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        var ref = new Object() {
            Id id;
            int refcount;
        };
        adapter.visit(new StreamCopier(getInput(), output) {

            @Override
            public void visitEntityHead() {
                ref.id = readId();
                ref.refcount = readInt();
                writeId(ref.id);
                writeInt(ref.refcount);
            }

            @Override
            public void visitEntity0(WireAdapter<?> adapter) {
                var t = visitEntity1(adapter);
                if (t.id() != null)
                    writeId(t.id());
                else
                    getOutput().write(t.data());
            }
        });
        var subTree = new Subtree(ref.id, parentId, bout.toByteArray(), -1);
        if (subTree.id() != null)
            add.accept(subTree);
        return subTree;
    }

}
