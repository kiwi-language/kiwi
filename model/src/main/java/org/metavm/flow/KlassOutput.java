package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.MvOutput;

import java.io.OutputStream;

@Slf4j
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

    @Override
    public void writeReference(Reference reference) {
        if (reference.tryGetId() == null)
            writeId(serializeContext.getId(reference.get()));
        else
            writeId(reference.getId());
    }

    @Override
    public void writeEntity(Entity entity) {
        if (entity.tryGetId() == null)
            entity.initId(serializeContext.getId(entity));
        super.writeEntity(entity);
    }

    @Override
    public void writeValue(Value value) {
        if (value instanceof Reference ref && ref.tryGetId() == null && ref.resolveDurable() instanceof Entity entity)
            entity.initId(serializeContext.getId(entity));
        super.writeValue(value);
    }

    public Id getId(Entity entity) {
        return serializeContext.getId(entity);
    }

}
