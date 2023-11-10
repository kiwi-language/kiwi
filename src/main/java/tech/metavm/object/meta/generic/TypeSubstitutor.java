package tech.metavm.object.meta.generic;

import tech.metavm.entity.Element;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.Entity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;

public class TypeSubstitutor extends ElementVisitor<Type> {

    private final Map<TypeVariable, Type> variableMap;
    private final IEntityContext entityContext;
    private final SaveTypeBatch batch;

    public TypeSubstitutor(List<TypeVariable> typeVariables,
                           List<? extends Type> typeArguments,
                           IEntityContext entityContext,
                           SaveTypeBatch batch) {
        this.entityContext = entityContext;
        this.variableMap = NncUtils.zip(typeVariables, typeArguments);
        this.batch = batch;
    }

    @Override
    public Type visitElement(Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type visitType(Type type) {
        return type;
    }

    @Override
    public Type visitArrayType(ArrayType type) {
        var elementType = type.getElementType().accept(this);
        return entityContext.getArrayTypeContext(type.getKind())
                .get(elementType, batch.getTmpId(new ArrayTypeKey(type.getKind().code(), elementType.getRef())));
    }

    @Override
    public Type visitUncertainType(UncertainType uncertainType) {
        var lb = uncertainType.getLowerBound().accept(this);
        var ub = uncertainType.getUpperBound().accept(this);
        return entityContext.getUncertainTypeContext().get(
                lb, ub, batch.getTmpId(new UncertainTypeKey(lb.getRef(), ub.getRef()))
        );
    }

    @Override
    public Type visitIntersectionType(IntersectionType type) {
        var types = NncUtils.map(type.getTypes(), t -> t.accept(this));
        return entityContext.getIntersectionTypeContext().get(
                types, batch.getTmpId(new IntersectionTypeKey(NncUtils.mapUnique(types, Type::getRef)))
        );
    }

    @Override
    public Type visitFunctionType(FunctionType functionType) {
        var paramTypes = NncUtils.map(functionType.getParameterTypes(), t -> t.accept(this));
        var returnType = functionType.getReturnType().accept(this);
        return entityContext.getFunctionTypeContext().get(
                paramTypes, returnType,
                batch.getTmpId(new FunctionTypeKey(NncUtils.map(paramTypes, Entity::getRef), returnType.getRef()))
        );
    }

    @Override
    public Type visitUnionType(UnionType type) {
        var members = NncUtils.mapUnique(type.getMembers(), t -> t.accept(this));
        return entityContext.getUnionTypeContext().get(
                members, batch.getTmpId(new UnionTypeKey(NncUtils.mapUnique(members, Entity::getRef)))
        );
    }

    @Override
    public Type visitTypeVariable(TypeVariable type) {
        return variableMap.getOrDefault(type, type);
    }

    @Override
    public Type visitClassType(ClassType type) {
        if (type.getTypeArguments().isEmpty()) {
            return type;
        } else {
            var typeArgs = NncUtils.map(type.getTypeArguments(), t -> t.accept(this));
            return entityContext.getGenericContext().getParameterizedType(
                    type.getEffectiveTemplate(),
                    typeArgs,
                    ResolutionStage.DECLARATION,
                    batch
            );
        }
    }

}
