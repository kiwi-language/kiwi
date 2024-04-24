package tech.metavm.object.type.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.type.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
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
        var subst = substitute(type);
        return subst != null ? subst : type.copy();
    }

    @Override
    public Type visitArrayType(ArrayType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        var elementType = type.getElementType().accept(this);
        return new ArrayType(null, elementType, type.getKind());
    }

    @Override
    public Type visitUncertainType(UncertainType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new UncertainType(null, type.getLowerBound().accept(this), type.getUpperBound().accept(this));
    }

    @Override
    public Type visitIntersectionType(IntersectionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new IntersectionType(null, NncUtils.mapUnique(type.getTypes(), t -> t.accept(this)));
    }

    @Override
    public Type visitFunctionType(FunctionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new FunctionType(
                null,
                NncUtils.map(type.getParameterTypes(), t -> t.accept(this)),
                type.getReturnType().accept(this)
        );
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new UnionType(null, NncUtils.mapUnique(type.getMembers(), t -> t.accept(this)));
    }

    @Override
    public Type visitClassType(ClassType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new ClassType(type.getKlass(), NncUtils.map(type.getTypeArguments(), t -> t.accept(this)));
    }

    private @Nullable Type substitute(Type type) {
        return variableMap.get(type);
    }

}
