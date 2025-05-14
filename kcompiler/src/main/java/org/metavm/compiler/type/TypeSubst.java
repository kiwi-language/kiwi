package org.metavm.compiler.type;

import org.metavm.compiler.analyze.DeferredType;
import org.metavm.compiler.element.TypeVar;
import org.metavm.compiler.util.List;
import org.metavm.util.Utils;

import java.util.IdentityHashMap;
import java.util.Map;

public class TypeSubst extends AbstractTypeVisitor<Type> {

    public static TypeSubst create(List<? extends Type> from, List<? extends Type> to) {
        if (from.isEmpty())
            return empty;
        return new TypeSubst(from, to);
    }

    public static TypeSubst create(Map<Type, Type> map) {
        if (map.isEmpty())
            return empty;
        return new TypeSubst(map);
    }

    private final Types types = Types.instance;
    private final Map<Type, Type> map = new IdentityHashMap<>();

    private TypeSubst(List<? extends Type> from, List<? extends Type> to) {
        Utils.require(from.size() == to.size(), () -> "Expecting " + from.size() + " type arguments, but got " + to.size());
        Utils.biForEach(from, to, map::put);
    }

    private TypeSubst(Map<Type, Type> map) {
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

    public static final TypeSubst empty = new TypeSubst(List.nil(), List.nil()) {

        @Override
        public Type visitFunctionType(FuncType funcType) {
            return funcType;
        }

        @Override
        public Type visitIntersectionType(IntersectionType intersectionType) {
            return intersectionType;
        }

        @Override
        public Type visitUncertainType(UncertainType uncertainType) {
            return uncertainType;
        }

        @Override
        public Type visitClassType(ClassType classTypeInst) {
            return classTypeInst;
        }

        @Override
        public Type visitArrayType(ArrayType arrayType) {
            return arrayType;
        }

        @Override
        public Type visitUnionType(UnionType unionType) {
            return unionType;
        }

        @Override
        public Type visitDeferredType(DeferredType deferredType) {
            return deferredType;
        }

        @Override
        public Type visitTypeVariable(TypeVar typeVar) {
            return typeVar;
        }

        @Override
        public Type visitPrimitiveType(PrimitiveType primitiveType) {
            return primitiveType;
        }

        @Override
        public Type visitType(Type type) {
            return type;
        }
    };

}
