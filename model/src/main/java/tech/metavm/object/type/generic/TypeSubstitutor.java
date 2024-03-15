package tech.metavm.object.type.generic;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.Entity;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends ElementVisitor<Type> {

    private final Map<Type, Type> variableMap;
    private final CompositeTypeFacade compositeTypeFacade;
    private final DTOProvider dtoProvider;

    public TypeSubstitutor(List<? extends Type> typeVariables,
                           List<? extends Type> typeArguments,
                           CompositeTypeFacade compositeTypeFacade,
                           DTOProvider dtoProvider) {
        this.compositeTypeFacade = compositeTypeFacade;
        this.variableMap = NncUtils.zip(typeVariables, typeArguments);
        this.dtoProvider = dtoProvider;
    }

    public void addMapping(TypeVariable from, Type to) {
        variableMap.put(from, to);
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
    public Type visitTypeVariable(TypeVariable type) {
        return super.visitTypeVariable(type);
    }

    @Override
    public Type visitClassType(ClassType type) {
        var subst = super.visitClassType(type);
        if (subst != type)
            return subst;
        if (type.getTypeArguments().isEmpty())
            return type;
        else {
            return compositeTypeFacade.getParameterizedType(
                    type.getEffectiveTemplate(),
                    NncUtils.map(type.getTypeArguments(), t -> t.accept(this)),
                    ResolutionStage.DECLARATION,
                    dtoProvider
            );
        }
    }

}
