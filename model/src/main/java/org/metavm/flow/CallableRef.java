package org.metavm.flow;

import org.metavm.entity.Reference;
import org.metavm.entity.ValueElement;

public abstract class CallableRef extends ValueElement implements Reference {

    public abstract Callable resolve();

}
