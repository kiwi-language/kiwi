package org.metavm.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.metavm.ddl.Commit;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeDef;
import org.metavm.util.InternalException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MetaContextCache extends EntityContextFactoryAware {

    public static final int MAX_SIZE = 16;

    private final LoadingCache<Long, IEntityContext> cache = CacheBuilder.newBuilder()
            .maximumSize(MAX_SIZE)
            .build(new CacheLoader<>() {

                @Override
                public @NotNull IEntityContext load(@NotNull Long key) {
                    return createMetaContext(key);
                }
            });

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MetaContextCache(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        Commit.META_CONTEXT_INVALIDATE_HOOK = this::invalidate;
    }

    public IEntityContext get(long appId) {
        try {
            return cache.get(appId);
        } catch (ExecutionException e) {
            throw new InternalException(e);
        }
    }

    public void invalidate(long appId) {
        cache.invalidate(appId);
    }

    private IEntityContext createMetaContext(long appId) {
        var context = newContext(appId);
        loadAllTypeDefs(context);
        return context;
    }

    private void loadAllTypeDefs(IEntityContext context) {
//        var future = executor.submit(() -> {
        var typeDefs = context.selectByKey(Klass.IDX_ALL_FLAG, true);
        for (TypeDef typeDef : typeDefs) {
            EntityUtils.ensureTreeInitialized(typeDef);
            if(typeDef instanceof Klass klass && klass.isEnum()) {
                klass.getEnumConstants();
            }
        }
//        });
//        try {
//            future.get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
    }

}