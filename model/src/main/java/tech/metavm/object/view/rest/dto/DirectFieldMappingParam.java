package tech.metavm.object.view.rest.dto;

public record DirectFieldMappingParam(
) implements FieldMappingParam {

    @Override
    public int getKind() {
        return 1;
    }
}
