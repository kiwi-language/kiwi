package tech.metavm.object.type.rest.dto;

public record CapturedTypeParam(String variableId) implements TypeParam {

    @Override
    public int getType() {
        return 10;
    }

}
