package org.metavm.object.type.rest.dto;

public interface TypeKeyVisitor<R> {

    R visitTypeKey(TypeKey typeKey);

    R visitClassTypeKey(ClassTypeKey typeKey);

    R visitIntersectionTypeKey(IntersectionTypeKey typeKey);

    R visitUnionTypeKey(UnionTypeKey typeKey);

    R visitPrimitiveTypeKey(PrimitiveTypeKey typeKey);

    R visitAnyTypeKey(AnyTypeKey typeKey);

    R visitNeverTypeKey(NeverTypeKey typeKey);

    R visitUncertainTypeKey(UncertainTypeKey typeKey);

    R visitFunctionTypeKey(FunctionTypeKey typeKey);

    R visitCapturedTypeKey(CapturedTypeKey typeKey);

    R visitVariableTypeKey(VariableTypeKey typeKey);

    R visitArrayTypeKey(ArrayTypeKey typeKey);

    R visitParameterizedTypeKey(ParameterizedTypeKey typeKey);

    R visitNullTypeKey(NullTypeKey typeKey);
}
