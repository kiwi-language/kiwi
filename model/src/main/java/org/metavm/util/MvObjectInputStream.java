package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ContextListener;
import org.metavm.object.instance.core.PrimitiveValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.TypeTags;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

@Slf4j
@Entity(ephemeral = true, isNative = true)
public class MvObjectInputStream extends ObjectInputStream {

    public static MvObjectInputStream create(InstanceInput input, IEntityContext context) {
        try {
            return new MvObjectInputStream(input, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final transient InstanceInput input;
    private final transient IEntityContext context;

    protected MvObjectInputStream(InstanceInput input, IEntityContext context) throws IOException {
        super();
        this.input = input;
        this.context = context;
    }

    @Override
    protected Object readObjectOverride() {
        var val = input.readValue();
        if(val instanceof PrimitiveValue primitiveValue)
            return primitiveValue.getValue();
        else if(val instanceof Reference reference) {
            var inst = reference.resolve();
            if (TypeTags.isNonArraySystemTypeTag(inst.getType().getTypeTag())) {
                ((ContextListener) context).onInstanceInitialized(inst);
                return context.getEntity(Object.class, inst);
            } else
                return val;
        }
        else
            throw new IllegalStateException("Invalid value: " + val);
    }

    @Override
    public void defaultReadObject() {
        var inst = (ClassInstance) Objects.requireNonNull(input.getParent());
        inst.defaultRead(input);
    }

    @Override
    public @NotNull String readUTF() {
        return input.readUTF();
    }

    @Override
    public long readLong() throws IOException {
        return input.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return input.readInt();
    }
}
