package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.TypeRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.type.*;
import tech.metavm.object.version.Version;
import tech.metavm.object.version.VersionRepository;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;

public class EntityInstanceContextBridge implements MappingProvider, ParameterizedFlowProvider,
        TypeProvider, IndexProvider, VersionRepository, TypeRegistry, CompositeTypeFacade {

    private IEntityContext entityContext;

    @Override
    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments) {
        return entityContext.getGenericContext().getParameterizedFlow(template, typeArguments);
    }

    @Override
    public <T extends Flow> T getExistingFlow(T template, List<? extends Type> typeArguments) {
        return entityContext.getGenericContext().getExistingFlow(template, typeArguments);
    }

    @Override
    public void add(Flow flow) {
        entityContext.getGenericContext().add(flow);
    }

    @Override
    public Mapping getMapping(Id id) {
        return entityContext.getMapping(id);
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public Type getType(Id id) {
        return entityContext.getType(id);
    }

    @Override
    public Index getIndex(Id id) {
        return entityContext.getEntity(Index.class, id);
    }

    @Nullable
    @Override
    public Version getLastVersion() {
        return NncUtils.first(
                entityContext.query(Version.IDX_VERSION.newQueryBuilder().limit(1).desc(true).build())
        );
    }

    @Override
    public void save(Version version) {
        entityContext.bind(version);
    }

    @Override
    public Type getType(Class<?> javaClass) {
        return entityContext.getDefContext().getType(javaClass);
    }

    @Override
    public ArrayType getArrayType(Type elementType, ArrayKind kind, @javax.annotation.Nullable Long tmpId) {
        return entityContext.getArrayTypeContext(kind).get(elementType, tmpId);
    }

    @Override
    public FunctionType getFunctionType(List<Type> parameterTypes, Type returnType, @javax.annotation.Nullable Long tmpId) {
        return entityContext.getFunctionTypeContext().get(NncUtils.append(parameterTypes, returnType), tmpId);
    }

    @Override
    public UnionType getUnionType(Set<Type> types, @javax.annotation.Nullable Long tmpId) {
        return entityContext.getUnionTypeContext().getUnionType(types, tmpId);
    }

    @Override
    public IntersectionType getIntersectionType(Set<Type> types, @javax.annotation.Nullable Long tmpId) {
        return entityContext.getIntersectionTypeContext().getIntersectionType(types, tmpId);
    }

    @Override
    public UncertainType getUncertainType(Type lowerBound, Type upperBound, @javax.annotation.Nullable Long tmpId) {
        return entityContext.getUncertainTypeContext().get(List.of(lowerBound, upperBound), tmpId);
    }

    @Override
    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage resolutionStage, DTOProvider dtoProvider) {
        return entityContext.getGenericContext().getParameterizedType(template, typeArguments, resolutionStage, dtoProvider);
    }
}
