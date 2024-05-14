package tech.metavm.object.type;

import tech.metavm.util.NncUtils;

public class StructuralTypeTransformer<S> extends TypeTransformer<S> {

    @Override
    public Type visitType(Type type, S s) {
        return type.copy();
    }

    @Override
    public Type visitClassType(ClassType type, S s) {
        return new ClassType(type.getKlass(), NncUtils.map(type.getTypeArguments(), t -> t.accept(this, s)));
    }

    @Override
    public Type visitArrayType(ArrayType type, S s) {
        return new ArrayType(type.getElementType(), type.getKind());
    }

    @Override
    public Type visitFunctionType(FunctionType type, S s) {
        return new FunctionType(NncUtils.map(type.getParameterTypes(), t -> t.accept(this, s)), type.getReturnType().accept(this, s));
    }

    @Override
    public Type visitUncertainType(UncertainType type, S s) {
        return new UncertainType(type.getLowerBound().accept(this, s), type.getUpperBound().accept(this, s));
    }

    @Override
    public Type visitUnionType(UnionType type, S s) {
        return new UnionType(NncUtils.mapUnique(type.getMembers(), t -> t.accept(this, s)));
    }

    @Override
    public Type visitIntersectionType(IntersectionType type, S s) {
        return new IntersectionType(NncUtils.mapUnique(type.getTypes(), t -> t.accept(this, s)));
    }

}
