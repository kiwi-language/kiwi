package org.metavm.compiler.type;

import org.metavm.util.Utils;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends AbstractTypeVisitor<Type> {

    private final Types types = Types.instance;
    private final Map<Type, Type> map = new IdentityHashMap<>();

    public TypeSubstitutor(List<? extends Type> from, List<? extends Type> to) {
        Utils.require(from.size() == to.size());
        Utils.biForEach(from, to, map::put);
    }

    @Override
    public Type visitType(Type type) {
        return map.getOrDefault(type, type);
    }

    @Override
    public Type visitClassType(ClassType classType) {
        var subst = super.visitClassType(classType);
        if (subst != classType)
            return subst;
        return classType.getClazz().getType(
                Utils.safeCall(classType.getOwner(), t -> (ClassType) t.accept(this)),
                classType.getTypeArguments().map(t -> t.accept(this))
        );
    }

    @Override
    public Type visitUncertainType(UncertainType uncertainType) {
        var subst = super.visitUncertainType(uncertainType);
        if (subst != uncertainType)
            return subst;
        return types.getUncertainType(
                uncertainType.getLowerBound().accept(this),
                uncertainType.getUpperBound().accept(this)
        );
    }

    @Override
    public Type visitIntersectionType(IntersectionType intersectionType) {
        var subst = super.visitIntersectionType(intersectionType);
        if (subst != intersectionType)
            return subst;
        return types.getIntersectionType(
                intersectionType.getBounds().map(t -> t.accept(this))
        );
    }

    @Override
    public Type visitFunctionType(FunctionType functionType) {
        var subst = super.visitFunctionType(functionType);
        if (subst != functionType)
            return subst;
        return types.getFunctionType(
                functionType.getParameterTypes().map(t -> t.accept(this)),
                functionType.getReturnType().accept(this)
        );
    }

    @Override
    public Type visitArrayType(ArrayType arrayType) {
        var subst = super.visitArrayType(arrayType);
        if (subst != arrayType)
            return subst;
        return types.getArrayType(arrayType.getElementType().accept(this));
    }

    @Override
    public Type visitUnionType(UnionType unionType) {
        var subst = super.visitUnionType(unionType);
        if (subst != unionType)
            return subst;
        return types.getUnionType(unionType.alternatives().map(t -> t.accept(this)));
    }

}
