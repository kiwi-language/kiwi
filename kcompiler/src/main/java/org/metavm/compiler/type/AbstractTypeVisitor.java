package org.metavm.compiler.type;

import org.metavm.compiler.element.TypeVariable;

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
    public R visitFunctionType(FunctionType functionType) {
        return visitType(functionType);
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
    public R visitTypeVariable(TypeVariable typeVariable) {
        return visitType(typeVariable);
    }

    @Override
    public R visitClassType(ClassType classType) {
        return visitType(classType);
    }
}
