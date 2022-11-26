package tech.metavm.flow;

import tech.metavm.entity.InstanceContext;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.meta.Type;

public abstract class Value {
//    protected final InstanceContext context;
    private final ValueKind kind;

    public Value(ValueDTO valueDTO/*, InstanceContext context*/) {
//        this.context = context;
        kind = ValueKind.getByCodeRequired(valueDTO.kind());
    }

    protected abstract Object getDTOValue(boolean persisting);

    public ValueDTO toDTO(boolean persisting) {
        return new ValueDTO(kind.code(), getDTOValue(persisting), persisting ? null :
                getType().toDTO()
        );
    }

    public abstract Type getType();

    public abstract Object evaluate(EvaluationContext evaluationContext);

}
