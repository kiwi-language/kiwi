package org.metavm.object.instance;

import org.metavm.object.instance.core.Value;

public interface ArrayListener {

    void onAdd(Value instance);

    void onRemove(Value instance);

}
