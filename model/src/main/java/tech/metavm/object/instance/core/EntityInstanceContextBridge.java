package tech.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.TypeRegistry;
import tech.metavm.object.type.*;
import tech.metavm.object.version.Version;
import tech.metavm.object.version.VersionRepository;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.NncUtils;

public class EntityInstanceContextBridge implements MappingProvider,
        TypeDefProvider, IndexProvider, VersionRepository, TypeRegistry {

    private IEntityContext entityContext;

    @Override
    public Mapping getMapping(Id id) {
        return entityContext.getMapping(id);
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public TypeDef getTypeDef(Id id) {
        return entityContext.getTypeDef(id);
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

}
