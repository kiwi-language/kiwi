package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.flow.MethodRef;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.MethodCpEntryDTO;

public class MethodCpEntry extends CpEntry {

    private final MethodRef methodRef;

    public MethodCpEntry(int index, MethodRef methodRef) {
        super(index);
        this.methodRef = methodRef;
    }

    @Override
    public MethodRef getValue() {
        return methodRef;
    }

    @Override
    public Object resolve() {
        return methodRef.resolve();
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new MethodCpEntryDTO(getIndex(), methodRef.toDTO(serializeContext));
    }
}
