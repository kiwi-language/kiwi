package tech.metavm.object.type.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends ElementVisitor<Type> {

    public static final Logger debugLoggerGER = LoggerFactory.getLogger("Debug");

    private final Map<Type, Type> variableMap;
    private final CompositeTypeFacade compositeTypeFacade;
    private final DTOProvider dtoProvider;

    public TypeSubstitutor(List<? extends Type> typeVariables,
                           List<? extends Type> typeArguments,
                           CompositeTypeFacade compositeTypeFacade,
                           DTOProvider dtoProvider) {
        this.compositeTypeFacade = compositeTypeFacade;
        if(typeVariables.size() != typeArguments.size())
            throw new InternalException("Type variables and type arguments have different sizes. " +
                    "type variables: [" + NncUtils.join(typeVariables, Type::getTypeDesc) + "]"
                    + ", type arguments: [" + NncUtils.join(typeArguments, Type::getTypeDesc) + "]");
        this.variableMap = new HashMap<>(NncUtils.zip(typeVariables, typeArguments));
        this.dtoProvider = dtoProvider;
    }

    public void addMapping(Type from, Type to) {
        variableMap.put(from, to);
        if(DebugEnv.debugging)
            debugLoggerGER.info("added mapping: {} -> {}", EntityUtils.getEntityDesc(from), EntityUtils.getEntityDesc(to));
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
        return compositeTypeFacade.getArrayType(
                elementType, type.getKind(),
                dtoProvider.getTmpId(new ArrayTypeKey(type.getKind().code(), elementType.getStringId()))
        );
    }

    @Override
    public Type visitUncertainType(UncertainType uncertainType) {
        var subst = super.visitUncertainType(uncertainType);
        if (subst != uncertainType)
            return subst;
        var lb = uncertainType.getLowerBound().accept(this);
        var ub = uncertainType.getUpperBound().accept(this);
        return compositeTypeFacade.getUncertainType(
                lb, ub, dtoProvider.getTmpId(new UncertainTypeKey(lb.getStringId(), ub.getStringId()))
        );
    }

    @Override
    public Type visitIntersectionType(IntersectionType type) {
        var subst = super.visitIntersectionType(type);
        if (subst != type)
            return subst;
        var types = NncUtils.mapUnique(type.getTypes(), t -> t.accept(this));
        return compositeTypeFacade.getIntersectionType(
                types, dtoProvider.getTmpId(new IntersectionTypeKey(NncUtils.mapUnique(types, Type::getStringId)))
        );
    }

    @Override
    public Type visitFunctionType(FunctionType functionType) {
        var subst = super.visitFunctionType(functionType);
        if (subst != functionType)
            return subst;
        var paramTypes = NncUtils.map(functionType.getParameterTypes(), t -> t.accept(this));
        var returnType = functionType.getReturnType().accept(this);
        return compositeTypeFacade.getFunctionType(
                paramTypes, returnType,
                dtoProvider.getTmpId(new FunctionTypeKey(NncUtils.map(paramTypes, Entity::getStringId), returnType.getStringId()))
        );
    }

    private void logVariableMap() {
        if(DebugEnv.debugging) {
            debugLoggerGER.info("variable map:");
            for (var entry : variableMap.entrySet()) {
                debugLoggerGER.info("key: {}, value: {}", EntityUtils.getEntityDesc(entry.getKey()), EntityUtils.getEntityDesc(entry.getValue()));
            }
        }
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var subst = super.visitUnionType(type);
        if (subst != type)
            return subst;
        var members = NncUtils.mapUnique(type.getMembers(), t -> t.accept(this));
        return compositeTypeFacade.getUnionType(
                members, dtoProvider.getTmpId(new UnionTypeKey(NncUtils.mapUnique(members, Entity::getStringId)))
        );
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
            return compositeTypeFacade.getParameterizedType(
                    type.getEffectiveTemplate(),
                    substitutedTypeArgs,
                    ResolutionStage.DECLARATION,
                    dtoProvider
            );
        }
    }

}
