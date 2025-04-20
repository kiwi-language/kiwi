package org.metavm.compiler.type;

import org.metavm.compiler.analyze.DeferredType;
import org.metavm.compiler.element.TypeVar;

public abstract class AbstractTypeVisitor<R> implements TypeVisitor<R> {

    @Override
    public R visitPrimitiveType(PrimitiveType primitiveType) {
        return visitType(primitiveType);
    }

    @Override
    public R visitUncertainType(UncertainType uncertainType) {
        return visitType(uncertainType);
    }

    @Override
    public R visitIntersectionType(IntersectionType intersectionType) {
        return visitType(intersectionType);
    }

    @Override
    public R visitFunctionType(FuncType funcType) {
        return visitType(funcType);
    }

    @Override
    public R visitArrayType(ArrayType arrayType) {
        return visitType(arrayType);
    }

    @Override
    public R visitUnionType(UnionType unionType) {
        return visitType(unionType);
    }

    @Override
    public R visitTypeVariable(TypeVar typeVar) {
        return visitType(typeVar);
    }

    @Override
    public R visitClassType(ClassType classTypeInst) {
        return visitType(classTypeInst);
    }

    @Override
    public R visitDeferredType(DeferredType deferredType) {
        return visitType(deferredType);
    }
}
