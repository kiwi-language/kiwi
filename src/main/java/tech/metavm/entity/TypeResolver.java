package tech.metavm.entity;

import tech.metavm.object.meta.Type;

public interface TypeResolver {

    Type getType(InstanceContext context, long typeId);

}
