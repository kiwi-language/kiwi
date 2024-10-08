package org.metavm.util;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;

import java.io.IOException;
import java.io.ObjectOutputStream;

@EntityType(ephemeral = true, isNative = true)
public class MvObjectOutputStream extends ObjectOutputStream {

    private final transient InstanceOutput out;
    private final transient IEntityContext context;

    public MvObjectOutputStream(InstanceOutput out, IEntityContext context) throws IOException {
        super();
        this.out = out;
        this.context = context;
    }

    @Override
    protected void writeObjectOverride(Object obj) {
        var inst = context.getInstance(obj);
        out.writeInstance(inst.getReference());
    }
}
