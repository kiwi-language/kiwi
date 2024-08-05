package org.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.TypeRegistry;
import org.metavm.object.type.*;
import org.metavm.object.version.Version;
import org.metavm.object.version.VersionRepository;
import org.metavm.object.view.Mapping;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.NncUtils;

public class EntityInstanceContextBridge implements MappingProvider,
        TypeDefProvider, IndexProvider, VersionRepository, TypeRegistry, RedirectStatusProvider, ActiveCommitProvider {

    private IEntityContext entityContext;

    @Override
    public Mapping getMapping(Id id) {
        return entityContext.getMapping(id);
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public RedirectStatus getRedirectStatus(Id id) {
        return entityContext.getRedirectStatus(id);
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

    @javax.annotation.Nullable
    @Override
    public Commit getActiveCommit() {
        return entityContext.selectFirstByKey(Commit.IDX_RUNNING, true);
    }
}
