package org.metavm.object.type.generic;

import org.metavm.flow.Flow;
import org.metavm.object.type.Type;
import org.metavm.util.IdentitySet;

import java.util.Set;

public class CompositeTypeEventRegistry {

    private final static ThreadLocal<CompositeTypeEventRegistry> TL = ThreadLocal.withInitial(CompositeTypeEventRegistry::new);

    private final Set<CompositeTypeListener> listeners = new IdentitySet<>();

    private CompositeTypeEventRegistry() {
    }

    public static void addListener(CompositeTypeListener listener) {
        TL.get().listeners.add(listener);
    }

    public static void removeListener(CompositeTypeListener listener) {
        TL.get().listeners.remove(listener);
    }

    public static void notifyTypeCreated(Type type) {
        for (CompositeTypeListener listener : TL.get().listeners) {
            listener.onTypeCreated(type);
        }
    }

    public static void notifyFlowCreated(Flow flow) {
        for (CompositeTypeListener listener : TL.get().listeners) {
            listener.onFlowCreated(flow);
        }
    }


}
