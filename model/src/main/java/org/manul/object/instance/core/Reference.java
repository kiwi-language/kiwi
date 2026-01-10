package org.manul.object.instance.core;

import org.manul.wire.Wire;

@Wire(adapter = ReferenceAdapter.class)
public interface Reference extends Value {

    Instance get();

    boolean isRemoved();

    boolean isResolved();

}
