package tech.metavm.object.type.generic;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.Entity;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends ElementVisitor<Type> {

    private final Map<TypeVariable, Type> variableMap;
    private final CompositeTypeFacade compositeTypeFacade;
    private final DTOProvider dtoProvider;

    public TypeSubstitutor(List<TypeVariable> typeVariables,
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
        return type;
    }

    @Override
    public Type visitArrayType(ArrayType type) {
        var elementType = type.getElementType().accept(this);
        return compositeTypeFacade.getArrayType(
                elementType, type.getKind(),
                dtoProvider.getTmpId(new ArrayTypeKey(type.getKind().code(), elementType.getRef()))
        );
    }

    @Override
    public Type visitUncertainType(UncertainType uncertainType) {
        var lb = uncertainType.getLowerBound().accept(this);
        var ub = uncertainType.getUpperBound().accept(this);
        return compositeTypeFacade.getUncertainType(
                lb, ub, dtoProvider.getTmpId(new UncertainTypeKey(lb.getRef(), ub.getRef()))
        );
    }

    @Override
    public Type visitIntersectionType(IntersectionType type) {
        var types = NncUtils.mapUnique(type.getTypes(), t -> t.accept(this));
        return compositeTypeFacade.getIntersectionType(
                types, dtoProvider.getTmpId(new IntersectionTypeKey(NncUtils.mapUnique(types, Type::getRef)))
        );
    }

    @Override
    public Type visitFunctionType(FunctionType functionType) {
        var paramTypes = NncUtils.map(functionType.getParameterTypes(), t -> t.accept(this));
        var returnType = functionType.getReturnType().accept(this);
        return compositeTypeFacade.getFunctionType(
                paramTypes, returnType,
                dtoProvider.getTmpId(new FunctionTypeKey(NncUtils.map(paramTypes, Entity::getRef), returnType.getRef()))
        );
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var members = NncUtils.mapUnique(type.getMembers(), t -> t.accept(this));
        return compositeTypeFacade.getUnionType(
                members, dtoProvider.getTmpId(new UnionTypeKey(NncUtils.mapUnique(members, Entity::getRef)))
        );
    }

    @Override
    public Type visitTypeVariable(TypeVariable type) {
        return variableMap.getOrDefault(type, type);
    }

    @Override
    public Type visitClassType(ClassType type) {
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
