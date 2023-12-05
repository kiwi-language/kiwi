package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class NewObjectParam extends CallParam implements NewParam<NewObjectParam> {

    @Nullable
    private final ParentRefDTO parent;

    private final boolean ephemeral;

    public NewObjectParam(@JsonProperty("flowRef") RefDTO flowRef,
                          @JsonProperty("typeRef") @Nullable RefDTO typeRef,
                          @JsonProperty("arguments") List<ArgumentDTO> arguments,
                          @Nullable @JsonProperty("parent") ParentRefDTO parent,
                          @JsonProperty("ephemeral") boolean ephemeral
    ) {
        super(flowRef, typeRef, arguments);
        this.parent = parent;
        this.ephemeral = ephemeral;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    @Nullable
    public ParentRefDTO getParent() {
        return parent;
    }

    @Override
    public NewObjectParam copyWithParentRef(ParentRefDTO parentRef) {
        return new NewObjectParam(
                getFlowRef(), getTypeRef(), getArguments(), parentRef, ephemeral
        );
    }
}
