package tech.metavm.object.type.rest.dto;

import javax.annotation.Nullable;

public record CapturedTypeParam(
        String scopeId,
        String uncertainTypeId,
        int index,
        long key,
        @Nullable String parameterId
) implements TypeParam {

    @Override
    public int getType() {
        return 10;
    }

    @Override
    public CapturedTypeKey getTypeKey() {
        return new CapturedTypeKey(
                scopeId,
                uncertainTypeId,
                index
        );
    }
}
