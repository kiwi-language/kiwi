package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.FieldCpEntryDTO;

public class FieldCpEntry extends CpEntry {

    private final FieldRef fieldRef;

    public FieldCpEntry(int index, FieldRef fieldRef) {
        super(index);
        this.fieldRef = fieldRef;
    }

    @Override
    public FieldRef getValue() {
        return fieldRef;
    }

    @Override
    public Object resolve() {
        return fieldRef.resolve();
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new FieldCpEntryDTO(getIndex(), fieldRef.toDTO(serializeContext));
    }
}
