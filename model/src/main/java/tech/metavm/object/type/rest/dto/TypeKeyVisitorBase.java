package tech.metavm.object.type.rest.dto;

public abstract class TypeKeyVisitorBase<R> implements TypeKeyVisitor<R> {

    @Override
    public R visitClassTypeKey(ClassTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitTaggedClassTypeKey(TaggedClassTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitIntersectionTypeKey(IntersectionTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitUnionTypeKey(UnionTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitPrimitiveTypeKey(PrimitiveTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitAnyTypeKey(AnyTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitNeverTypeKey(NeverTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitUncertainTypeKey(UncertainTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitFunctionTypeKey(FunctionTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitCapturedTypeKey(CapturedTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitVariableTypeKey(VariableTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitArrayTypeKey(ArrayTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }

    @Override
    public R visitParameterizedTypeKey(ParameterizedTypeKey typeKey) {
        return visitTypeKey(typeKey);
    }
}
