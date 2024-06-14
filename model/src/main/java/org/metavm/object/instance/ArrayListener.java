package org.metavm.object.instance;

import org.metavm.object.instance.core.Instance;

public interface ArrayListener {

    void onAdd(Instance instance);

    void onRemove(Instance instance);

}
