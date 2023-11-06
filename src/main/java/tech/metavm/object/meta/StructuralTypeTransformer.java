package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.util.NncUtils;

import java.util.List;

public class StructuralTypeTransformer<S> extends TypeTransformer<S> {

    private final IEntityContext entityContext;

    public StructuralTypeTransformer(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public Type visitClassType(ClassType type, S s) {
        List<Type> typeArgs = visit(type.getTypeArguments(), s);
        if(typeArgs.equals(type.getTypeArguments())) {
            return type;
        }
        else {
            return entityContext.getParameterizedType(
                    NncUtils.requireNonNull(type.getTemplate()),
                    typeArgs
            );
        }
    }

    @Override
    public Type visitArrayType(ArrayType type, S s) {
        var elementType = visit(type.getElementType(), s);
        if(elementType.equals(type.getElementType())) {
            return type;
        }
        else {
            return entityContext.getArrayType(elementType, type.getKind());
        }
    }

    @Override
    public Type visitFunctionType(FunctionType type, S s) {
        var paramTypes = visit(type.getParameterTypes(), s);
        var returnType = visit(type.getReturnType(), s);
        if(paramTypes.equals(type.getParameterTypes()) && returnType.equals(type.getReturnType())) {
            return type;
        }
        else {
            return entityContext.getFunctionType(paramTypes, returnType);
        }
    }

    @Override
    public Type visitUncertainType(UncertainType type, S s) {
        var lowerBound = visit(type.getLowerBound(), s);
        var upperBound = visit(type.getUpperBound(), s);
        if(lowerBound.equals(type.getLowerBound()) && upperBound.equals(type.getUpperBound())) {
            return type;
        }
        else {
            return entityContext.getUncertainType(lowerBound, upperBound);
        }
    }

    @Override
    public Type visitUnionType(UnionType type, S s) {
        var members = visit(type.getMembers(), s);
        if(members.equals(type.getMembers())) {
            return type;
        }
        else {
            return entityContext.getUnionType(members);
        }
    }

    @Override
    public Type visitIntersectionType(IntersectionType type, S s) {
        var types = visit(type.getTypes(), s);
        if(types.equals(type.getTypes())) {
            return type;
        }
        else {
            return entityContext.getIntersectionType(types);
        }
    }

}
