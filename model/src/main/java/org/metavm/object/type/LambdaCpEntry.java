package org.metavm.object.type;

import org.metavm.entity.SerializeContext;
import org.metavm.flow.LambdaRef;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.object.type.rest.dto.LambdaCpEntryDTO;

public class LambdaCpEntry extends CpEntry {

    private final LambdaRef lambdaRef;

    public LambdaCpEntry(int index, LambdaRef lambdaRef) {
        super(index);
        this.lambdaRef = lambdaRef;
    }

    @Override
    public LambdaRef getValue() {
        return lambdaRef;
    }

    @Override
    public Object resolve() {
        return lambdaRef.resolve();
    }

    @Override
    public CpEntryDTO toDTO(SerializeContext serializeContext) {
        return new LambdaCpEntryDTO(getIndex(), lambdaRef.toDTO(serializeContext));
    }
}
