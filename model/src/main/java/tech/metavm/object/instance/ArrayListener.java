package tech.metavm.object.instance;

import tech.metavm.object.instance.core.Instance;

public interface ArrayListener {

    void onAdd(Instance instance);

    void onRemove(Instance instance);

}
