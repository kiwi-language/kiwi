package org.metavm.object.type.generic;

import org.metavm.flow.Flow;
import org.metavm.object.type.Type;

public interface CompositeTypeListener {

    void onTypeCreated(Type type);

    void onFlowCreated(Flow flow);

}
