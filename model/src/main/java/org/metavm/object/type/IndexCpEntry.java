package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.IndexCpEntryDTO;

public class IndexCpEntry extends CpEntry {

    private final IndexRef indexRef;

    public IndexCpEntry(int index, IndexRef indexRef) {
        super(index);
        this.indexRef = indexRef;
    }

    @Override
    public IndexRef getValue() {
        return indexRef;
    }

    @Override
    public Object resolve() {
        return indexRef.resolve();
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new IndexCpEntryDTO(getIndex(), indexRef.toDTO(serializeContext));
    }
}
