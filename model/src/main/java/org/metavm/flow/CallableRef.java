package org.metavm.flow;

import org.metavm.entity.Reference;
import org.metavm.entity.ValueElement;
import org.metavm.object.type.TypeMetadata;

public abstract class CallableRef extends ValueElement implements Reference {

    public abstract TypeMetadata getTypeMetadata();
}
