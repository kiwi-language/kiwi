package org.metavm.compiler.type;

import org.metavm.compiler.util.List;
import org.metavm.util.Utils;

import java.util.IdentityHashMap;
import java.util.Map;

public class TypeSubst extends AbstractTypeVisitor<Type> {

    public static final TypeVisitor<Type> empty = new TypeSubst(List.nil(), List.nil());
    private final Types types = Types.instance;
    private final Map<Type, Type> map = new IdentityHashMap<>();

    public TypeSubst(List<? extends Type> from, List<? extends Type> to) {
        Utils.require(from.size() == to.size());
        Utils.biForEach(from, to, map::put);
    }

    public TypeSubst(Map<Type, Type> map) {
        this.map.putAll(map);
    }

    @Override
    public Type visitType(Type type) {
        return map.getOrDefault(type, type);
    }

    @Override
    public Type visitClassType(ClassType classTypeInst) {
        var subst = super.visitClassType(classTypeInst);
        if (subst != classTypeInst)
            return subst;
        return classTypeInst.getClazz().getInst(
                Utils.safeCall(classTypeInst.getOwner(), t -> (ClassType) t.accept(this)),
                classTypeInst.getTypeArguments().map(t -> t.accept(this))
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
    public Type visitFunctionType(FuncType funcType) {
        var subst = super.visitFunctionType(funcType);
        if (subst != funcType)
            return subst;
        return types.getFuncType(
                funcType.getParamTypes().map(t -> t.accept(this)),
                funcType.getRetType().accept(this)
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
