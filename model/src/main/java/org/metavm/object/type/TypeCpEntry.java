package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.TypeCpEntryDTO;

public class TypeCpEntry extends CpEntry {

    private final Type type;

    public TypeCpEntry(int index, Type type) {
        super(index);
        this.type = type;
    }

    @Override
    public Type getValue() {
        return type;
    }

    @Override
    public Object resolve() {
        return type;
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new TypeCpEntryDTO(getIndex(), type.toExpression(serializeContext));
    }
}
