package org.metavm.entity;

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

public class SubtreeExtractor extends StreamVisitor {

    private Id parentId;
    private long parentFieldTag = -1L;
    private final Consumer<Subtree> add;

    public SubtreeExtractor(InputStream in, Consumer<Subtree> add) {
        super(in);
        this.add = add;
    }

    @Override
    public void visitRecordBody(long nodeId, TypeOrTypeKey typeOrTypeKey) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.RECORD);
        output.writeLong(nodeId);
        typeOrTypeKey.write(output);
        var oldParentId = parentId;
        var oldParentFieldTag = parentFieldTag;
        var id = PhysicalId.of(getTreeId(), nodeId, typeOrTypeKey);
        parentId = id;
        parentFieldTag = -1;
        new StreamCopier(getInput(), output) {
            @Override
            public void visitField() {
                writeLong(parentFieldTag = readLong());
                visit();
            }

            @Override
            public void visitRecord() {
                var nodeId = readLong();
                var typeKey = readTypeKey();
                write(WireTypes.REFERENCE);
                writeId(PhysicalId.of(getTreeId(), nodeId, typeKey));
                SubtreeExtractor.this.visitRecordBody(nodeId, typeKey);
            }
        }.visitRecordBody(nodeId, typeOrTypeKey);
        parentId = oldParentId;
        parentFieldTag = oldParentFieldTag;
        add.accept(new Subtree(
                id,
                parentId,
                parentFieldTag,
                bout.toByteArray()
        ));
    }

}
