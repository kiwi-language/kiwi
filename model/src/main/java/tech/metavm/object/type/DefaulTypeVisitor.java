package tech.metavm.object.type;

public abstract class DefaulTypeVisitor<R, S> implements TypeVisitor<R, S> {

    public final R visit(Type type, S s) {
        return type.accept(this, s);
    }

    @Override
    public R visitClassType(ClassType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitArrayType(ArrayType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitUnionType(UnionType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitTypeVariable(TypeVariable type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitIntersectionType(IntersectionType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitFunctionType(FunctionType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitUncertainType(UncertainType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitPrimitiveType(PrimitiveType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitNothingType(NeverType type, S s) {
        return visitType(type, s);
    }

    @Override
    public R visitObjectType(AnyType type, S s) {
        return visitType(type, s);
    }
}
