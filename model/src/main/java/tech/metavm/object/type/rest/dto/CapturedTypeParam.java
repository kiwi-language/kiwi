package tech.metavm.object.type.rest.dto;

public record CapturedTypeParam(
        String scopeId,
        String uncertainTypeId,
        int index
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
