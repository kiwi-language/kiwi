package org.metavm.object.type.generic;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityUtils;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    "type variables: [" + Utils.join(typeVariables, Type::getTypeDesc) + "]"
                    + ", type arguments: [" + Utils.join(typeArguments, Type::getTypeDesc) + "]");
        this.variableMap = new HashMap<>(Utils.zip(typeVariables, typeArguments));
    }

    public void addMapping(Type from, Type to) {
        variableMap.put(from, to);
        if (DebugEnv.debugging)
            debugLogger.info("added mapping: {} -> {}", EntityUtils.getEntityDesc(from), EntityUtils.getEntityDesc(to));
    }

    @Override
    public Type visitElement(Element element) {
        throw new UnsupportedOperationException();
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
        return new IntersectionType(Utils.mapToSet(type.getTypes(), t -> t.accept(this)));
    }

    @Override
    public Type visitFunctionType(FunctionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new FunctionType(
                Utils.map(type.getParameterTypes(), t -> t.accept(this)),
                type.getReturnType().accept(this)
        );
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new UnionType(Utils.mapToSet(type.getMembers(), t -> t.accept(this)));
    }

    @Override
    public Type visitStringType(StringType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return type;
    }

    @Override
    public Type visitKlassType(KlassType type) {
        var subst = substitute(type);
        if (subst != null)
            return subst;
        return new KlassType(
                (ClassType) Utils.safeCall(type.getOwner(), t -> t.accept(this)),
                type.getKlass(), Utils.map(type.getTypeArguments(), t -> t.accept(this)));
    }

    @Override
    public Type visitMethodRef(MethodRef methodRef) {
        return super.visitMethodRef(methodRef);
    }

    @Override
    public Type visitFunctionRef(FunctionRef functionRef) {
        return super.visitFunctionRef(functionRef);
    }

    private @Nullable Type substitute(Type type) {
        return variableMap.get(type);
    }

    Map<Type, Type> getVariableMap() {
        return Collections.unmodifiableMap(variableMap);
    }

}
