package tech.metavm.entity;

import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.type.Type;

public interface TypeResolver {

    Type getType(InstanceContext context, long typeId);

}
