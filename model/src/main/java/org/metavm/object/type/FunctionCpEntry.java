package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.flow.FunctionRef;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.FunctionCpEntryDTO;

public class FunctionCpEntry extends CpEntry {

    private final FunctionRef functionRef;

    public FunctionCpEntry(int index, FunctionRef functionRef) {
        super(index);
        this.functionRef = functionRef;
    }

    @Override
    public FunctionRef getValue() {
        return functionRef;
    }

    @Override
    public Object resolve() {
        return functionRef.resolve();
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new FunctionCpEntryDTO(getIndex(), functionRef.toDTO(serializeContext));
    }

}
