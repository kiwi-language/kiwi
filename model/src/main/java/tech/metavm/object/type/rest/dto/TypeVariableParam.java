package tech.metavm.object.type.rest.dto;

public record TypeVariableParam(
        String variableId
) implements TypeParam {
    @Override
    public int getType() {
        return 5;
    }

}
