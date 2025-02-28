package org.metavm.object.instance.core;

public interface Reference extends Value {

    Instance get();

    boolean isRemoved();

    boolean isResolved();

}
