package tech.metavm.flow.rest;

import java.util.List;
import java.util.Objects;

public class UnresolvedNewObjectNodeParam extends UnresolvedCallNodeParam {

    private final boolean ephemeral;

    private final boolean unbound;

    public UnresolvedNewObjectNodeParam(String flowName, String typeId, List<ValueDTO> arguments, boolean ephemeral, boolean unbound) {
        super(flowName, typeId, arguments);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    @Override
    public String getTypeId() {
        return Objects.requireNonNull(super.getTypeId());
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isUnbound() {
        return unbound;
    }

    @Override
    public int getCallKind() {
        return 1;
    }
}
