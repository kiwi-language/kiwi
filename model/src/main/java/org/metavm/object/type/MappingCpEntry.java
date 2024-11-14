package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.MappingCpEntryDTO;
import org.metavm.object.view.ObjectMappingRef;

public class MappingCpEntry extends CpEntry {

    private final ObjectMappingRef mappingRef;

    protected MappingCpEntry(int index, ObjectMappingRef mappingRef) {
        super(index);
        this.mappingRef = mappingRef;
    }

    @Override
    public Object getValue() {
        return mappingRef;
    }

    @Override
    public Object resolve() {
        return mappingRef.resolve();
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new MappingCpEntryDTO(getIndex(), mappingRef.toDTO(serializeContext));
    }
}
