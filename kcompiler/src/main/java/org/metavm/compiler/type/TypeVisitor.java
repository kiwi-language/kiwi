package org.metavm.compiler.type;

import org.metavm.compiler.analyze.DeferredType;
import org.metavm.compiler.element.TypeVar;

public interface TypeVisitor<R> {

    R visitType(Type type);

    R visitClassType(ClassType classType);

    R visitPrimitiveType(PrimitiveType primitiveType);


    R visitUncertainType(UncertainType uncertainType);

    R visitIntersectionType(IntersectionType intersectionType);

    R visitFunctionType(FuncType funcType);

    R visitArrayType(ArrayType arrayType);

    R visitUnionType(UnionType unionType);

    R visitTypeVariable(TypeVar typeVar);

    R visitDeferredType(DeferredType deferredType);
}
