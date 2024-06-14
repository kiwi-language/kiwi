package org.metavm.flow;

import org.metavm.entity.Reference;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueElement;
import org.metavm.flow.rest.CallableRefDTO;

public abstract class CallableRef extends ValueElement implements Reference {

    public abstract CallableRefDTO toDTO(SerializeContext serializeContext);

    public abstract Callable resolve();

}
