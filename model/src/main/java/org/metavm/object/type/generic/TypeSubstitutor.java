package org.metavm.object.type.generic;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityUtils;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        return subst != null ? subst : type;
    }

    @Override
    public Type visitArrayType(ArrayType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        var elementType = type.getElementType().accept(this);
        return new ArrayType(elementType, type.getKind());
    }

    @Override
    public Type visitUncertainType(UncertainType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new UncertainType(type.getLowerBound().accept(this), type.getUpperBound().accept(this));
    }

    @Override
    public Type visitIntersectionType(IntersectionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new IntersectionType(NncUtils.mapUnique(type.getTypes(), t -> t.accept(this)));
    }

    @Override
    public Type visitFunctionType(FunctionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new FunctionType(
                NncUtils.map(type.getParameterTypes(), t -> t.accept(this)),
                type.getReturnType().accept(this)
        );
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new UnionType(NncUtils.mapUnique(type.getMembers(), t -> t.accept(this)));
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

    Map<Type, Type> getVariableMap() {
        return Collections.unmodifiableMap(variableMap);
    }

}
