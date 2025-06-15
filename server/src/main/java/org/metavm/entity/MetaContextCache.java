package org.metavm.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.ddl.Commit;
import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MetaContextCache extends EntityContextFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(MetaContextCache.class);

    public static final int MAX_SIZE = 16;

    private final LoadingCache<CacheKey, IInstanceContext> cache = CacheBuilder.newBuilder()
            .maximumSize(MAX_SIZE)
            .build(new CacheLoader<>() {

                @Override
                public @NotNull IInstanceContext load(@NotNull CacheKey key) {
                    return createMetaContext(key);
                }
            });

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MetaContextCache(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        Commit.META_CONTEXT_INVALIDATE_HOOK = this::invalidate;
    }

    public IInstanceContext get(long appId) {
        return get(appId, false);
    }

    public IInstanceContext get(long appId, boolean migrating) {
        try {
            var context = cache.get(new CacheKey(appId, migrating));
            ParameterizedStore.setMap(context.getParameterizedMap());
            return context;
        } catch (ExecutionException e) {
            throw new InternalException(e);
        }
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
            if (context.getAppId() > Constants.PLATFORM_APP_ID) {
                BeanDefinitionRegistry.getInstance(context);
                try {
                    KlassTagAssigner.getInstance(context);
                }
                catch (Exception e) {
                    throw e;
                }
                KlassSourceCodeTagAssigner.getInstance(context);
            }
            var klasses = context.loadKlasses();
            for (Klass klass : klasses) {
                var sft = StaticFieldTable.getInstance(klass.getType(), context);
                if (klass.isEnum())
                    sft.getEnumConstants();
            }
            context.setParameterizedMap(ParameterizedStore.getMap());
            if (DebugEnv.dumpMetaContext) {
                logger.trace("MetaContext Dump");
                context.dumpContext();
            }
            return context;
        }
    }

    private record CacheKey(long appId, boolean migrating) {
    }

}