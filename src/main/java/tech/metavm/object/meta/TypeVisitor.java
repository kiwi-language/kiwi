package tech.metavm.object.meta;

public interface TypeVisitor<R,S> {

    R visitType(Type type, S s);

    R visitClassType(ClassType type, S s);

    R visitArrayType(ArrayType type, S s);

    R visitUnionType(UnionType type, S s);

    R visitTypeVariable(TypeVariable type, S s);

    R visitIntersectionType(IntersectionType type, S s);

    R visitFunctionType(FunctionType type, S s);

    R visitUncertainType(UncertainType type, S s);

    R visitPrimitiveType(PrimitiveType type, S s);

    R visitNothingType(NothingType type, S s);

    R visitObjectType(ObjectType type, S s);

}
