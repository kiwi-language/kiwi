package org.metavm.flow;

import org.metavm.entity.Entity;
import org.metavm.entity.SerializeContext;
import org.metavm.util.MvOutput;

import java.io.OutputStream;

public class KlassOutput extends MvOutput {

    private final SerializeContext serializeContext;

    public KlassOutput(OutputStream out, SerializeContext serializeContext) {
        super(out);
        this.serializeContext = serializeContext;
    }

    @Override
    public void writeEntityId(Entity entity) {
        writeId(serializeContext.getId(entity));
    }
}
