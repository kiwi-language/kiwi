package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ContextListener;
import org.metavm.object.type.TypeTags;

import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
@EntityType(ephemeral = true, isNative = true)
public class MvObjectInputStream extends ObjectInputStream {

    private final transient InstanceInput input;
    private final transient IEntityContext context;

    protected MvObjectInputStream(InstanceInput input, IEntityContext context) throws IOException {
        super();
        this.input = input;
        this.context = context;
    }

    @Override
    protected Object readObjectOverride() {
        var inst = input.readValue().resolveDurable();
        if(TypeTags.isNonArraySystemTypeTag(inst.getType().getTypeTag())) {
            ((ContextListener) context).onInstanceInitialized(inst);
            return context.getEntity(Object.class, inst);
        }
        else
            return inst.getReference();
    }
}
