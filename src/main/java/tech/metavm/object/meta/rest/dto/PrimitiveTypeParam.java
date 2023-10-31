package tech.metavm.object.meta.rest.dto;

public record PrimitiveTypeParam(
        int kind
) implements TypeParam{
    @Override
    public int getType() {
        return 3;
    }
}
