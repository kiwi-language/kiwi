package tech.metavm.flow.rest;

public record NewArrayParam(
        ValueDTO value,
        ParentRefDTO parentRef
) implements NewParam<NewArrayParam> {
    @Override
    public NewArrayParam copyWithParentRef(ParentRefDTO parentRef) {
        return new NewArrayParam(value, parentRef);
    }
}
