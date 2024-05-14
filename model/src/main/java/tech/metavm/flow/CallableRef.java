package tech.metavm.flow;

import tech.metavm.entity.Reference;
import tech.metavm.entity.SerializeContext;
import tech.metavm.entity.ValueElement;
import tech.metavm.flow.rest.CallableRefDTO;

public abstract class CallableRef extends ValueElement implements Reference {

    public abstract CallableRefDTO toDTO(SerializeContext serializeContext);

    public abstract Callable resolve();

    public abstract CallableRef copy();

}
