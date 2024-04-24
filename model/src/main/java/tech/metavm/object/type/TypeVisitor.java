package tech.metavm.object.type;

public interface TypeVisitor<R,S> {

    R visitType(Type type, S s);

    R visitClassType(ClassType type, S s);

    R visitArrayType(ArrayType type, S s);

    R visitUnionType(UnionType type, S s);

    R variableVariableType(VariableType type, S s);

    R visitIntersectionType(IntersectionType type, S s);

    R visitFunctionType(FunctionType type, S s);

    R visitUncertainType(UncertainType type, S s);

    R visitPrimitiveType(PrimitiveType type, S s);

    R visitNothingType(NeverType type, S s);

    R visitObjectType(AnyType type, S s);

}
