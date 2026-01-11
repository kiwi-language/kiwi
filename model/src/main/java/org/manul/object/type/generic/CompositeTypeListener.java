package org.manul.object.type.generic;

import org.manul.flow.Flow;
import org.manul.object.type.Type;

public interface CompositeTypeListener {

    void onTypeCreated(Type type);

    void onFlowCreated(Flow flow);

}
