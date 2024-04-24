package tech.metavm.object.type.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.type.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends ElementVisitor<Type> {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final Map<Type, Type> variableMap;

    public TypeSubstitutor(List<? extends Type> typeVariables,
                           List<? extends Type> typeArguments) {
        if (typeVariables.size() != typeArguments.size())
            throw new InternalException("Type variables and type arguments have different sizes. " +
                    "type variables: [" + NncUtils.join(typeVariables, Type::getTypeDesc) + "]"
                    + ", type arguments: [" + NncUtils.join(typeArguments, Type::getTypeDesc) + "]");
        this.variableMap = new HashMap<>(NncUtils.zip(typeVariables, typeArguments));
    }

    public void addMapping(Type from, Type to) {
        variableMap.put(from, to);
        if (DebugEnv.debugging)
            debugLogger.info("added mapping: {} -> {}", EntityUtils.getEntityDesc(from), EntityUtils.getEntityDesc(to));
    }

    @Override
    public Type visitType(Type type) {
        return variableMap.getOrDefault(type, type);
    }

    @Override
    public Type visitArrayType(ArrayType type) {
        var subst = super.visitArrayType(type);
        if (subst != type)
            return subst;
        var elementType = type.getElementType().accept(this);
        return new ArrayType(null, elementType, type.getKind());
    }

    @Override
    public Type visitUncertainType(UncertainType uncertainType) {
        var subst = super.visitUncertainType(uncertainType);
        if (subst != uncertainType)
            return subst;
        return new UncertainType(null, uncertainType.getLowerBound().accept(this), uncertainType.getUpperBound().accept(this));
    }

    @Override
    public Type visitIntersectionType(IntersectionType type) {
        var subst = super.visitIntersectionType(type);
        if (subst != type)
            return subst;
        return new IntersectionType(null, NncUtils.mapUnique(type.getTypes(), t -> t.accept(this)));
    }

    @Override
    public Type visitFunctionType(FunctionType functionType) {
        var subst = super.visitFunctionType(functionType);
        if (subst != functionType)
            return subst;
        return new FunctionType(
                null,
                NncUtils.map(functionType.getParameterTypes(), t -> t.accept(this)),
                functionType.getReturnType().accept(this)
        );
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var subst = super.visitUnionType(type);
        if (subst != type)
            return subst;
        return new UnionType(null, NncUtils.mapUnique(type.getMembers(), t -> t.accept(this)));
    }


    @Override
    public Type visitClassType(ClassType type) {
        var subst = super.visitClassType(type);
        if (subst != type)
            return subst;
        if (type.getTypeArguments().isEmpty())
            return type;
        else {
            var substitutedTypeArgs = NncUtils.map(type.getTypeArguments(), t -> t.accept(this));
            return new ClassType(type.getKlass(), substitutedTypeArgs);
        }
    }

}
