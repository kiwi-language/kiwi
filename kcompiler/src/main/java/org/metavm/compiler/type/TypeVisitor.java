package org.metavm.compiler.type;

import org.metavm.compiler.element.TypeVariable;

public interface TypeVisitor<R> {

    R visitType(Type type);

    R visitClassType(ClassType classType);

    R visitPrimitiveType(PrimitiveType primitiveType);


    R visitUncertainType(UncertainType uncertainType);

    R visitIntersectionType(IntersectionType intersectionType);

    R visitFunctionType(FunctionType functionType);

    R visitArrayType(ArrayType arrayType);

    R visitUnionType(UnionType unionType);

    R visitTypeVariable(TypeVariable typeVariable);
}
