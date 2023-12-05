package tech.metavm.object.type.rest.dto;

public record PrimitiveTypeParam(
        int kind
) implements TypeParam {
    @Override
    public int getType() {
        return 3;
    }

    @Override
    public TypeKey getTypeKey() {
        return new PrimitiveTypeKey(kind);
    }
}
