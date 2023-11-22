package tech.metavm.entity;

import tech.metavm.util.InstanceOutput;
import tech.metavm.util.StreamCopier;
import tech.metavm.util.StreamVisitor;
import tech.metavm.util.WireTypes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;

public class SubtreeExtractor extends StreamVisitor {

    private long parentId = -1L;
    private long parentFieldId = -1L;
    private final Consumer<Subtree> add;

    public SubtreeExtractor(InputStream in, Consumer<Subtree> add) {
        super(in);
        this.add = add;
    }

    @Override
    public void visitRecordBody(long id) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.write(WireTypes.RECORD);
        output.writeLong(id);
        var oldParentId = parentId;
        var oldParentFieldId = parentFieldId;
        parentId = id;
        parentFieldId = -1L;
        new StreamCopier(getInput(), output) {
            @Override
            public void visitField() {
                writeLong(parentFieldId = readLong());
                visit();
            }

            @Override
            public void visitRecord() {
                long id = readLong();
                write(WireTypes.REFERENCE);
                writeLong(id);
                SubtreeExtractor.this.visitRecordBody(id);
            }
        }.visitRecordBody(id);
        parentId = oldParentId;
        parentFieldId = oldParentFieldId;
        add.accept(new Subtree(
                id,
                parentId,
                parentFieldId,
                bout.toByteArray()
        ));
    }

}
