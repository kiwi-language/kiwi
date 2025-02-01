package org.metavm.object.type;

public interface TypeVisitor<R,S> {

    R visitType(Type type, S s);

    R visitKlassType(KlassType type, S s);

    R visitStringType(StringType type, S s);

    R visitArrayType(ArrayType type, S s);

    R visitUnionType(UnionType type, S s);

    R visitVariableType(VariableType type, S s);

    R visitCapturedType(CapturedType type, S s);

    R visitIntersectionType(IntersectionType type, S s);

    R visitFunctionType(FunctionType type, S s);

    R visitUncertainType(UncertainType type, S s);

    R visitPrimitiveType(PrimitiveType type, S s);

    R visitNeverType(NeverType type, S s);

    R visitAnyType(AnyType type, S s);

    R visitNullType(NullType type, S s);

}
