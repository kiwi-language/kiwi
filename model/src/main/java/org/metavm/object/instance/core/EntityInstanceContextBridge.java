package org.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.metavm.ddl.Commit;
import org.metavm.entity.DefContext;
import org.metavm.entity.DefContextProvider;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.TypeRegistry;
import org.metavm.object.type.*;
import org.metavm.object.version.Version;
import org.metavm.object.version.VersionRepository;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

public class EntityInstanceContextBridge implements
        TypeDefProvider, IndexProvider, VersionRepository, TypeRegistry, RedirectStatusProvider, ActiveCommitProvider, DefContextProvider {

    private IInstanceContext entityContext;

    public void setEntityContext(IInstanceContext entityContext) {
        this.entityContext = entityContext;
    }

    @Override
    public RedirectStatus getRedirectStatus(Id id) {
        return entityContext.getRedirectStatus(id);
    }

    @Override
    public ITypeDef getTypeDef(Id id) {
        return entityContext.getTypeDef(id);
    }

    @Override
    public Index getIndex(Id id) {
        return entityContext.getEntity(Index.class, id);
    }

    @Nullable
    @Override
    public Version getLastVersion() {
        return Utils.first(
                entityContext.query(Version.IDX_VERSION.newQueryBuilder().limit(1).desc(true).build())
        );
    }

    @Override
    public void save(Version version) {
        entityContext.bind(version);
    }

    @Override
    public Type getType(Class<?> javaClass) {
        return ModelDefRegistry.getType(javaClass);
    }

    @javax.annotation.Nullable
    @Override
    public Commit getActiveCommit() {
        return entityContext.selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
    }

    public DefContext getDefContext() {
        return ModelDefRegistry.getDefContext();
    }

}
