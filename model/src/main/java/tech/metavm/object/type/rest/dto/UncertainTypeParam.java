package tech.metavm.object.type.rest.dto;

public record UncertainTypeParam(
        String lowerBoundId,
        String upperBoundId
) implements TypeParam {

    @Override
    public int getType() {
        return 7;
    }

}
