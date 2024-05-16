package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.StreamCopier;
import tech.metavm.util.StreamVisitor;
import tech.metavm.util.WireTypes;

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
    public void visitRecordBody(Id id) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.RECORD);
        output.writeId(id);
        var oldParentId = parentId;
        var oldParentFieldTag = parentFieldTag;
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
                var id = readRecordId();
                write(WireTypes.REFERENCE);
                writeId(id);
                SubtreeExtractor.this.visitRecordBody(id);
            }
        }.visitRecordBody(id);
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
