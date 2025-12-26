package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.ddl.Commit;
import org.metavm.context.Component;
import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.ContextUtil;
import org.metavm.util.DebugEnv;
import org.metavm.util.LoadingCache;

@Component
@Slf4j
public class MetaContextCache extends EntityContextFactoryAware {

//    public static final int MAX_SIZE = 16;

    private final LoadingCache<CacheKey, IInstanceContext> cache = new LoadingCache<>(this::createMetaContext);


    public MetaContextCache(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        Commit.META_CONTEXT_INVALIDATE_HOOK = this::invalidate;
    }

    public IInstanceContext get(long appId) {
        return get(appId, false);
    }

    public IInstanceContext get(long appId, boolean migrating) {
        return cache.get(new CacheKey(appId, migrating));
    }

    public void invalidate(long appId, boolean migrating) {
        cache.invalidate(new CacheKey(appId, migrating));
    }

    private IInstanceContext createMetaContext(CacheKey key) {
        try(var ignored = ContextUtil.getProfiler().enter("createMetaContext")) {
            IInstanceContext context;
            if (key.migrating) {
                context = newContext(key.appId, builder -> builder.instanceStore(
                        mapperReg -> new InstanceStore(mapperReg, "instance_tmp", "index_entry_tmp"))
                );
            } else
                context = newContext(key.appId);
            if (key.migrating)
                context.setDescription("MigratingMetaContext");
            else
                context.setDescription("MetaContext");
            context.loadKlasses();
            if (DebugEnv.dumpMetaContext) {
                log.trace("MetaContext Dump");
                context.dumpContext();
            }
            return context;
        }
    }

    private record CacheKey(long appId, boolean migrating) {
    }

}