package tech.metavm.object.type.generic;

import tech.metavm.flow.Flow;
import tech.metavm.object.type.Type;

public interface CompositeTypeListener {

    void onTypeCreated(Type type);

    void onFlowCreated(Flow flow);

}
