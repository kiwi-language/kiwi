package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.wire.DefaultWireOutput;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Type;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class MvOutput extends DefaultWireOutput {

    protected MvOutput(OutputStream out) {
        super(out);
    }

    public void writeUTF(String s) {
        var bytes = s.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        write(bytes);
    }

    public void writeId(Id id) {
        id.write(this);
    }

    public void writeReference(Reference reference) {
        writeId(((EntityReference) reference).getId());
    }

    public void writeIdTag(IdTag idTag) {
        write(idTag.code());
    }


    public void writeInstance(Value value) {
        value.writeInstance(this);
    }

    public void writeValue(Value value) {
        value.write(this);
    }

    public void writeType(Type type) {
        type.write(this);
    }

}
