package org.manul.entity;

import org.manul.object.instance.core.InstanceContext;
import org.manul.object.type.Type;

public interface TypeResolver {

    Type getType(InstanceContext context, long typeId);

}
