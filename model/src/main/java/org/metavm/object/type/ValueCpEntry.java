package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.ValueCpEntryDTO;

public class ValueCpEntry extends CpEntry {

    private final Value value;

    public ValueCpEntry(int index, Value value) {
        super(index);
        this.value = value;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public Object resolve() {
        return value;
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new ValueCpEntryDTO(getIndex(), value.toFieldValueDTO());
    }
}
