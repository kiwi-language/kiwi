package org.metavm.entity;

import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.type.Type;

public interface TypeResolver {

    Type getType(InstanceContext context, long typeId);

}
