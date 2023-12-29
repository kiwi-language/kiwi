package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.TypeRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexProvider;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.version.Version;
import tech.metavm.object.version.VersionRepository;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingProvider;

import java.util.List;

public class InstanceContextDependency implements MappingProvider, ParameterizedFlowProvider,
        TypeProvider, IndexProvider, VersionRepository, TypeRegistry {

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
    public Mapping getMapping(RefDTO ref) {
        return entityContext.getMapping(ref);
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public Type getType(RefDTO ref) {
        return entityContext.getType(ref);
    }

    @Override
    public Index getIndex(RefDTO ref) {
        return entityContext.getEntity(Index.class, ref);
    }

    @Nullable
    @Override
    public Version getLastVersion() {
        return null;
    }

    @Override
    public void save(Version version) {
        entityContext.bind(version);
    }

    @Override
    public Type getType(Class<?> javaClass) {
        return entityContext.getDefContext().getType(javaClass);
    }
}
