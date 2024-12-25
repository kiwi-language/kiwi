package org.metavm.flow;

import org.metavm.entity.Reference;
import org.metavm.object.type.TypeMetadata;

public interface CallableRef extends Reference {

    TypeMetadata getTypeMetadata();

    Code getCode();

    FlowRef getFlow();

}
