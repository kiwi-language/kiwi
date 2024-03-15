package tech.metavm.object.type.rest.dto;

public record UncertainTypeParam(
        String lowerBoundId,
        String upperBoundId
) implements TypeParam {

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public TypeKey getTypeKey() {
        return new UncertainTypeKey(lowerBoundId, upperBoundId);
    }
}
