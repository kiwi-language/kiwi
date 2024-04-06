package tech.metavm.object.type.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends ElementVisitor<Type> {

    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("Debug");

    private final Map<Type, Type> variableMap;
    private final CompositeTypeFacade compositeTypeFacade;
    private final DTOProvider dtoProvider;

    public TypeSubstitutor(List<? extends Type> typeVariables,
                           List<? extends Type> typeArguments,
                           CompositeTypeFacade compositeTypeFacade,
                           DTOProvider dtoProvider) {
        this.compositeTypeFacade = compositeTypeFacade;
        this.variableMap = new HashMap<>(NncUtils.zip(typeVariables, typeArguments));
        this.dtoProvider = dtoProvider;
    }

    public void addMapping(Type from, Type to) {
        variableMap.put(from, to);
        if(DebugEnv.DEBUG_LOG_ON)
            DEBUG_LOGGER.info("added mapping: {} -> {}", EntityUtils.getEntityDesc(from), EntityUtils.getEntityDesc(to));
    }

    @Override
    public Type visitType(Type type) {
//        if(type instanceof CapturedType capturedType && capturedType.getScope().getScopeName().equals("find") && variableMap.size() == 2) {
//            System.out.println("Caught");
//        }
        var subst =  variableMap.getOrDefault(type, type);
        if(DebugEnv.DEBUG_LOG_ON) {
            DEBUG_LOGGER.info("substituting type {} to {}", EntityUtils.getEntityDesc(type), EntityUtils.getEntityDesc(subst));
            logVariableMap();
        }
        return subst;
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
        if(DebugEnv.DEBUG_LOG_ON && functionType.isCaptured()) {
            DEBUG_LOGGER.info("substituting function type: {}, id: {}", EntityUtils.getEntityDesc(functionType), functionType.getStringId());
            logVariableMap();
            DEBUG_LOGGER.info("current parameter types: {}", NncUtils.join(functionType.getParameterTypes(), EntityUtils::getEntityDesc));
            DEBUG_LOGGER.info("substituted parameter types: {}", NncUtils.join(paramTypes, EntityUtils::getEntityDesc));
            DEBUG_LOGGER.info("current return type: {}", EntityUtils.getEntityDesc(functionType.getReturnType()));
            DEBUG_LOGGER.info("substituted return type: {}", EntityUtils.getEntityDesc(returnType));
        }
        return compositeTypeFacade.getFunctionType(
                paramTypes, returnType,
                dtoProvider.getTmpId(new FunctionTypeKey(NncUtils.map(paramTypes, Entity::getStringId), returnType.getStringId()))
        );
    }

    private void logVariableMap() {
        if(DebugEnv.DEBUG_LOG_ON) {
            DEBUG_LOGGER.info("variable map:");
            for (var entry : variableMap.entrySet()) {
                DEBUG_LOGGER.info("key: {}, value: {}", EntityUtils.getEntityDesc(entry.getKey()), EntityUtils.getEntityDesc(entry.getValue()));
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
            if(DebugEnv.DEBUG_LOG_ON && type.isCaptured()) {
                DEBUG_LOGGER.info("substituting class type: {}", EntityUtils.getEntityDesc(type));
                logVariableMap();
                DEBUG_LOGGER.info("current type arguments: {}", NncUtils.join(type.getTypeArguments(), EntityUtils::getEntityDesc));
                DEBUG_LOGGER.info("substituted type arguments: {}", NncUtils.join(substitutedTypeArgs, EntityUtils::getEntityDesc));
            }
            return compositeTypeFacade.getParameterizedType(
                    type.getEffectiveTemplate(),
                    substitutedTypeArgs,
                    ResolutionStage.DECLARATION,
                    dtoProvider
            );
        }
    }

}
