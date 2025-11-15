package org.metavm.object.instance.core;

import org.metavm.wire.Wire;

@Wire(adapter = ReferenceAdapter.class)
public interface Reference extends Value {

    Instance get();

    boolean isRemoved();

    boolean isResolved();

}
